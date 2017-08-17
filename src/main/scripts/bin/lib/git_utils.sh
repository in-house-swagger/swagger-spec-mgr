#!/bin/bash
#set -eux
#==================================================================================================
# gitユーティリティ
#
# 前提
#   ・setenv.sh を事前に読み込んでいること
#
# 定義リスト
#   ・git.clone
#   ・git.fecth
#   ・git.reset
#   ・git.switch
#   ・git.pull
#   ・git.pull_rebase
#   ・git.status
#   ・git.staging
#   ・git.staging_clear
#   ・git.commit
#   ・git.staging_and_commit
#   ・git.revert
#   ・git.push
#   ・git.staging_and_push
#   ・git.log
#   ・git.create_repository
#   ・git.archive
#   ・git.archive_diff
#   ・git.diff_commit_list
#   ・git.commit_diff_file_list
#   ・git.status_diff_file_list
#   ・git.diff_file_list
#   ・git.commit_diff_file_details
#   ・git.status_diff_file_details
#   ・git.diff_file_details
#   ・git.branch_add
#   ・git.branch_rename
#   ・git.branch_remove
#   ・git.branch_merge
#   ・git.tag_add_local
#   ・git.tag_add
#   ・git.tag_rename_local
#   ・git.tag_rename
#   ・git.tag_remove_local
#   ・git.tag_remove
#   ・git.is_exist_tag
#   ・git.housekeep_local_repository
#
#==================================================================================================
#--------------------------------------------------------------------------------------------------
# 依存スクリプト読込み
#--------------------------------------------------------------------------------------------------
# ログ出力ユーティリティ
. ${DIR_BIN_LIB}/logging_utils.sh
# GitLab操作ユーティリティ
. ${DIR_BIN_LIB}/gitlab_utils.sh



#--------------------------------------------------------------------------------------------------
# 概要
#   Git作業ディレクトリの妥当性を確認します。
#
# 引数
#   ・1: Git作業ディレクトリ
#
# 戻り値
#    0: OK
#    6: NG
#
#--------------------------------------------------------------------------------------------------
function git.local.check_work_dir() {
  # Git作業ディレクトリ
  local _work_dir="$1"
  if [ ! -d ${_work_dir} ]; then
    log.error_console "${_work_dir} は存在しません。"
    return ${EXITCODE_ERROR}
  fi
  if [ ! -d ${_work_dir}/.git ]; then
    log.error_console "${_work_dir} はGit作業ディレクトリではありません。"
    return ${EXITCODE_ERROR}
  fi
}



#--------------------------------------------------------------------------------------------------
# 概要
#   Gitリポジトリをcloneします。
#   フェッチ除外ブランチ名状況にマッチするものを除いて、全てのブランチを追跡します。
#
# 引数
#   ・1: Git作業ディレクトリルート
#   ・2: グループディレクトリ      ※任意
#   ・3: クローンURL
#
# 設定
#   ・BRANCH_FETCH_IGNORE: フェッチ除外ブランチ名条件
#
# 戻り値
#    0: 取得できた場合
#    6: エラー発生時
#
# 出力
#   クローン先ディレクトリのパス
#
#--------------------------------------------------------------------------------------------------
function git.clone() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} [-g] DIR_WORK_ROOT [DIR_GROUP] CLONE_URL"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # グローバルコンフィグ使用有無
  local _is_use_global_config=false

  # オプション解析
  while :; do
    case $1 in
      -g|--use-global-config)
        _is_use_global_config=true
        shift
        ;;
      --)
        shift
        break
        ;;
      -*)
        log.error_console "${_USAGE}"
        log.remove_indent
        return ${EXITCODE_ERROR}
        ;;
      *)
        break
        ;;
    esac
  done

  # 引数の数
  if [ $# -lt 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリルート
  local _work_dir_root="$1"
  if [ ! -d ${_work_dir_root} ]; then
    log.error_console "${_work_dir_root} は存在しません。"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # グループ
  local _group_dir=
  if [ "`echo $2 | grep .git$`" = "" ]; then
    # 第2引数がURLではない場合
    _group_dir="$2"
    shift

    if [ ! -d ${_work_dir_root}/${_group_dir} ]; then
      log.debug_console "mkdir -p ${_work_dir_root}/${_group_dir}"
      mkdir -p ${_work_dir_root}/${_group_dir}
    fi
  fi

  # URL
  local _clone_url="$2"

  # プラグイン
  local _trg_dir=${DIR_PLUGIN}/common/${FUNCNAME[0]}

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  local _clone_dir
  if [ "${_group_dir}" != "" ]; then
    # グループが指定されている場合 ※ グループディレクトリ配下に移動
    _clone_dir=${_work_dir_root}/${_group_dir}
  else
    # グループが設定されてない場合 ※ Git作業ディレクトリルートに移動
    _clone_dir=${_work_dir_root}
  fi

  # git作業ディレクトリ
  local _work_dir=${_clone_dir}/`basename ${_clone_url%.*}`

  log.debug_console "cd ${_clone_dir}"
  cd "${_clone_dir}"

  # clone実行
  log.debug_console "git clone ${_clone_url}"
  log.add_indent
  git clone ${_clone_url}                                                                     2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "クローン に失敗しました。Git作業ディレクトリルート：${_work_dir_root}、グループ：${_group_dir}、クローンURL：${_clone_url}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 全てのブランチをトラッキング
  log.debug_console "git branch -r"
  log.add_indent
  cd ${_work_dir}
  for _remote_branch in `git branch -r | grep -v "HEAD" | sed -e 's| ||g'`; do
    local _target_local_branch=`echo ${_remote_branch} | sed -e 's|origin/||'`

    # fetch除外判定
    local _ignored_branch=`echo ${_target_local_branch} | grep -v ${BRANCH_FETCH_IGNORE}`
    if [ "${_ignored_branch}" = "" ]; then
      log.debug_console "${_target_local_branch} ブランチは除外ルール「${BRANCH_FETCH_IGNORE}」にマッチしたため、スキップします。"
      continue
    fi

    # すでに作成済みかチェック
    git branch | grep "${_target_local_branch}$" > /dev/null 2>&1
    local _is_exist=$?
    if [ ${_is_exist} -eq ${EXITCODE_SUCCESS} ]; then
      log.debug_console "${_target_local_branch} ブランチは既にローカルリポジトリで作成されているため、スキップします。"
      continue
    fi

    # トラッキング開始
    log.debug_console "git branch --track ${_target_local_branch} ${_remote_branch}"
    log.add_indent
    git branch --track ${_target_local_branch} ${_remote_branch}                              2>&1 |
    log.debug_console
    _ret_code=${PIPESTATUS[0]}
    log.remove_indent

    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.error_console "トラッキング開始 に失敗しました。Git作業ディレクトリ：${_work_dir}、対象ブランチ：${_remote_branch}、リターンコード：${_ret_code}"
      log.remove_indent
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

  done
  log.remove_indent

  # plugin実行
  if [ "${_is_use_global_config}" != "true" ]; then
    # global config を使用しない場合 ※ リポジトリ毎に config を設定

    export PATH_LOG
    log.debug_console "find ${_trg_dir} -maxdepth 1 -follow -type f -name \*.sh | sort"
    log.add_indent
    for _cur_file_path in `find ${_trg_dir} -maxdepth 1 -follow -type f -name \*.sh | sort`; do
      local _cur_file_name=`basename ${_cur_file_path}`
      local _cur_file_relpath=`echo ${_cur_file_path} | sed -e "s|${DIR_PLUGIN}/||"`

      # plugin実行
      log.debug_console "${_cur_file_path} ${_work_dir}"
      log.add_indent
      ${_cur_file_path} "${_work_dir}"
      _ret_code=$?
      log.remove_indent

      # 戻り値を確認
      if [ ${_ret_code} -eq ${EXITCODE_ERROR} ]; then
        log.error_console "${_cur_file_relpath} でエラーが発生しました。リターンコード：${_ret_code}"
        log.remove_indent
        log.remove_indent
        return ${EXITCODE_ERROR}
      fi

    done
  fi
  log.remove_indent

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   指定のブランチに対してフェッチを実行します。
#   ブランチが指定されていない場合、全てのブランチをフェッチします。
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2  : ブランチ            ※任意
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.fecth() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR [TARGET_BRANCH]"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -lt 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ブランチ ※未指定の場合は、全ブランチが対象になる
  local _branch="$2"

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # フェッチ
  log.debug_console "git fetch --prune origin ${_branch}"
  log.add_indent
  git fetch --prune origin ${_branch}                                                         2>&1 | #
  log.debug_console                                                                                  # stdout, stderr どちらもログ表示
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    # リトライ
    log.warn_console "フェッチに失敗したのでリトライを実施します。Git作業ディレクトリ：${_work_dir}、ブランチ:${_target_local_branch}、リターンコード：${_ret_code}"
    sleep 1
    log.debug_console "[RETRY] git fetch --prune origin ${_branch}"
    log.add_indent
    git fetch --prune origin ${_branch}                                                       2>&1 | #
    log.debug_console                                                                                # stdout, stderr どちらもログ表示
    _ret_code=${PIPESTATUS[0]}                                                                       # パイプの1つめの戻り値を取得
    log.remove_indent

    # 実行結果チェック
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.error_console "フェッチに失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ：${_branch}、リターンコード：${_ret_code}"
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi
  fi

  # リモートブランチ分ループ
  log.debug_console "git branch -r"
  log.add_indent
  for _remote_branch in `git branch -r | grep -v "HEAD" | sed -e 's| ||g'`; do
    local _target_local_branch=`echo ${_remote_branch} | sed -e 's|origin/||'`
    # 取得結果をチェック
    if [ "${_target_local_branch}" = "" ]; then
      # 空文字の場合、スキップ
      continue
    fi

    # fetch除外判定
    local _ignored_branch=`echo ${_target_local_branch} | grep -v ${BRANCH_FETCH_IGNORE}`
    if [ "${_ignored_branch}" = "" ]; then
      log.debug_console "${_target_local_branch} ブランチは除外ルール「${BRANCH_FETCH_IGNORE}」にマッチしたため、スキップします。"
      continue
    fi

    # すでに作成済みかチェック
    git branch | grep "${_target_local_branch}$" > /dev/null 2>&1
    local _is_exist=$?
    if [ ${_is_exist} -ne ${EXITCODE_SUCCESS} ]; then
      # 作成されてない場合

      # トラッキング開始
      log.debug_console "git branch --track ${_target_local_branch} ${_remote_branch}"
      log.add_indent
      git branch --track ${_target_local_branch} ${_remote_branch}                            2>&1 |
      log.debug_console
      _ret_code=${PIPESTATUS[0]}
      log.remove_indent

      # 実行結果チェック
      if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
        log.error_console "トラッキング開始 に失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ:${_target_local_branch}、リターンコード：${_ret_code}"
        log.remove_indent
        log.remove_indent
        return ${EXITCODE_ERROR}
      fi
    fi

    # フェッチ
    log.debug_console "git fetch origin ${_target_local_branch}"
    log.add_indent
    git fetch origin ${_target_local_branch}                                                  2>&1 |
    log.debug_console
    _ret_code=${PIPESTATUS[0]}                                                                       # パイプの1つめの戻り値を取得
    log.remove_indent

    # 実行結果チェック
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then

      # リトライ
      log.warn_console "フェッチに失敗したのでリトライを実施します。Git作業ディレクトリ：${_work_dir}、ブランチ:${_target_local_branch}、リターンコード：${_ret_code}"
      sleep 1
      log.debug_console "[RETRY] git fetch origin ${_target_local_branch}"
      log.add_indent
      git fetch origin ${_target_local_branch}                                                2>&1 |
      log.debug_console
      _ret_code=${PIPESTATUS[0]}                                                                     # パイプの1つめの戻り値を取得
      log.remove_indent

      # 実行結果チェック
      if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
        log.error_console "フェッチに失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ:${_target_local_branch}、リターンコード：${_ret_code}"
        log.remove_indent
        return ${EXITCODE_ERROR}
      fi
    fi

  done
  log.remove_indent

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   Git作業ディレクトリでの変更全て破棄します。
#
# 引数
#   ・1: Git作業ディレクトリ
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.local.reset_only() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # checkout時点のコミットにリセット
  log.debug_console "git reset --hard"
  log.add_indent
  git reset --hard                                                                            2>&1 |
  log.debug_console
  log.remove_indent

  # 新規作成ファイルの削除
  log.debug_console "git clean -df"
  log.add_indent
  git clean -df                                                                               2>&1 |
  log.debug_console
  log.remove_indent

  # ステータス確認
  log.debug_console "git status -s"
  log.add_indent
  git status -s                                                                               2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "Git作業ディレクトリのリセット に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}
}


#--------------------------------------------------------------------------------------------------
# 概要
#   Git作業ディレクトリでの変更全て破棄し、remoteの最新を取得します。
#
# 引数
#   ・1: Git作業ディレクトリ
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.reset() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # checkout時点のコミットにリセット
  git.local.reset_only ${_work_dir}
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リモートリポジトリ存在チェック
  git remote | grep 'origin'                                                                         > /dev/null 2>&1
  _ret_code=$?
  if [ ${_ret_code} -eq ${EXITCODE_SUCCESS} ]; then
    # リモートリポジトリが存在する場合

    # 作業ディレクトリを最新化
    git.pull "${_work_dir}"
    _ret_code=$?
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   指定のブランチに切り替えます。
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2  : ブランチ
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.switch() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR TARGET_BRANCH"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ブランチ
  local _branch=$2

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # 切替前にフェッチ実行
  git.fecth "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ブランチの存在チェック
  local _get_result=
  log.debug_console "git branch -a | grep ${_branch}"
  log.add_indent
  _get_result=`git branch -a                                                                  2>&1 | #
               grep ${_branch}`                                                                      # 対象のブランチに絞る
  _ret_code=${PIPESTATUS[0]}
  echo "${_get_result}"                                                                            |
  log.debug_console
  log.remove_indent

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ブランチ情報の取得に失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ：${_branch}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 存在チェック
  if [ "${_get_result}" = "" ]; then
    log.error_console "ブランチ：${_branch} は存在しません。Git作業ディレクトリ：${_work_dir}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ブランチ切替
  log.debug_console "git checkout ${_branch}"
  log.add_indent
  git checkout ${_branch}                                                                     2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ブランチの切替に失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ：${_branch}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   カレントブランチでリモートの最新を取得します。
#
# 引数
#   ・1  : git作業ディレクトリ
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.pull() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # 事前にフェッチ実行
  git.fecth "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "フェッチに失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ：$2、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # ブランチ設定
  log.debug_console "git rev-parse --abbrev-ref HEAD"
  log.add_indent
  local _branch=`git rev-parse --abbrev-ref HEAD`
  _ret_code=$?
  log.debug_console "${_branch}"
  log.remove_indent
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "カレントブランチの取得に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # pull
  log.debug_console "git pull origin ${_branch}"
  log.add_indent
  git pull origin ${_branch}                                                                  2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "プルに失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ：${_branch}、リターンコード：${_ret_code}"
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   カレントブランチでリモートの最新を取得します。
#   git作業ディレクトリでの編集を、リモートリポジトリの
#   最新コミットから修正したものとして置き換えます。
#
#
# 引数
#   ・1  : git作業ディレクトリ
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.pull_rebase() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ブランチ ※Git作業ディレクトリに移動後に設定
  local _branch=

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # 事前にフェッチ実行
  git.fecth "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "フェッチに失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ：$2、リターンコード：${_ret_code}"
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # ブランチ設定
  log.debug_console "git rev-parse --abbrev-ref HEAD"
  log.add_indent
  local _branch=`git rev-parse --abbrev-ref HEAD`
  _ret_code=$?
  log.debug_console "${_branch}"
  log.remove_indent
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "カレントブランチの取得に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # プル
  log.debug_console "git pull --rebase origin ${_branch}"
  log.add_indent
  git pull --rebase origin ${_branch}                                                         2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "プルに失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ：${_branch}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   作業ディレクトリの変更を状況を表示します。
#
# 引数
#   ・1  : git作業ディレクトリ
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.status() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -lt 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # ステータス表示
  log.info_console "Git作業ディレクトリ: `pwd`"
  log.info_console "カレントブランチ   : `git rev-parse --abbrev-ref HEAD`"
  log.debug_console "git status -s -uall"
  log.add_indent
  git status -s -uall                                                                         2>&1 |
  log.info_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   作業ディレクトリの変更をステージングエリアに追加します。
#   追加対象パスの指定がない場合は作業ディレクトリ内の全変更（新規追加・削除含む）が追加されます。
#   追加対象パスを指定している場合は指定パスのみ追加されさます。
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2～: 追加対象パス        ※任意
#
# 戻り値
#    0: 正常終了の場合
#    3: ステージング対象が存在しない場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.staging() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR [TARGET_PATH1 ...]"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -lt 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # ステージング対象の存在確認
  log.debug_console "git status -s"
  _status_result=`git status -s`
  _ret_code=$?
  if [ ${_ret_code} -eq ${EXITCODE_SUCCESS} -a "${_status_result}" = "" ]; then
    log.remove_indent
    return ${EXITCODE_WARN}
  fi

  # 対象ファイルをステージング
  shift
  if [ $# -eq 0 ]; then
    # 追加対象のパス指定が無い場合 ※全変更内容(新規追加・削除を含む)をステージング
    log.debug_console "git add -A"
    log.add_indent
    git add -A                                                                                2>&1 |
    log.debug_console
    _ret_code=${PIPESTATUS[0]}                                                                       # パイプの1つめの戻り値を取得
    log.remove_indent

    # 実行結果チェック
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.error_console "ステージングに失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

  else
    # 追加対象のパス指定の有る場合 ※指定パスのみステージング
    while [ $# -gt 0 ]; do
      local _cur_staging_target=$1
      if [ -e ${_cur_staging_target} ]; then
        # 新規追加・更新の場合
        log.debug_console "git add ${_cur_staging_target}"
        log.add_indent
        git add ${_cur_staging_target}                                                        2>&1 |
        log.debug_console
        log.remove_indent

      else
        # 削除の場合
        log.debug_console "git rm --cached ${_cur_staging_target}"
        log.add_indent
        git rm --cached ${_cur_staging_target}                                                2>&1 |
        log.debug_console
        log.remove_indent
      fi

      _ret_code=${PIPESTATUS[0]}                                                                     # パイプの1つめの戻り値を取得

      # 実行結果チェック
      if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
        log.error_console "ステージングに失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
        log.remove_indent
        return ${EXITCODE_ERROR}
      fi

      shift
    done
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   ステージングエリアに追加されている変更をクリアします。
#   クリア対象パスの指定がない場合は全変更（新規追加・削除含む）をクリアします。
#   クリア対象パスを指定している場合は指定パスのみクリアします。
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2～: クリア対象パス      ※任意
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.staging_clear() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR [TARGET_PATH1 ...]"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -lt 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # クリア対象ファイルをステージングエリアからクリア
  shift
  if [ $# -eq 0 ]; then
    # クリア対象のパス指定が無い場合 ※ステージングされている全変更内容(新規追加・削除を含む)をクリア
    log.debug_console "git reset HEAD"
    log.add_indent
    git reset HEAD                                                                            2>&1 |
    log.debug_console
    log.remove_indent
  else
    # クリア対象のパス指定の有る場合 ※指定パスのみクリア
    log.debug_console "git reset HEAD $@"
    log.add_indent
    git reset HEAD $@                                                                         2>&1 |
    log.debug_console
    log.remove_indent
  fi

  _ret_code=${PIPESTATUS[0]}

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ステージングのクリアに失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   commitを実行します。
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2  : コミットコメント
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.commit() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR COMMIT_COMMENT"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # コミットコメント
  local _commit_comment="$2"

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # コミット
  log.debug_console "git commit -m \"${_commit_comment}\" "
  log.add_indent
  git commit -m "${_commit_comment}"                                                          2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "コミットに失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   Git作業ディレクトリの編集内容を正にしたcommit
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2  : コミットメッセージ
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.staging_and_commit() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR COMMIT_COMMENT"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # コミットメッセージ
  local _message="$2"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  # ステージング
  git.staging ${_work_dir}
  local _cur_ret_code=$?
  if [ ${_cur_ret_code} -eq ${EXITCODE_WARN} ]; then
    # ステージング対象が存在しない場合、スキップ
    log.debug_console "ステージング対象が存在しませんでした。"
    log.remove_indent
    return ${EXITCODE_SUCCESS}

  elif [ ${_cur_ret_code} -eq ${EXITCODE_ERROR} ]; then
    log.error_console "ステージングでエラーが発生しました。リターンコード：${_cur_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # コミット
  git.commit ${_work_dir} "${_message}"
  _cur_ret_code=$?
  if [ ${_cur_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "コミットでエラーが発生しました。リターンコード：${_cur_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   指定のコミットを打ち消します。
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2  : 打ち消し対象のコミットハッシュ
#
# オプション
#   -m : マージコミット打ち消しオプション
#        マージコミットを打ち消す場合に指定します。
#        親が複数存在する場合は、1つ目のマージコミットを残します。
#
#   -m2: マージコミット打ち消しオプション
#        親が複数存在する場合に、2つ目のマージコミットを残します。
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.revert() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} [-m|-m2] GIT_WORK_DIR COMMIT_HASH"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  local _is_merge_commmit=false
  local _is_merge_commmit2=false

  # オプション解析
  while :; do
    case $1 in
      -m)
        _is_merge_commmit=true
        shift
        ;;
      -m2)
        _is_merge_commmit2=true
        shift
        ;;
      --)
        shift
        break
        ;;
      -*)
        log.error_console "${_USAGE}"
        log.remove_indent
        return ${EXITCODE_ERROR}
        ;;
      *)
        break
        ;;
    esac
  done

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # オプション確認
  if [ "${_is_merge_commmit}" = "true" -a "${_is_merge_commmit2}" = "true" ]; then
    log.error_console "-m と -m2 オプションは一方しか指定できません。"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # コミットハッシュ
  local _commit_hash="$2"

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # revert
  if [ "${_is_merge_commmit}" != "true" -a "${_is_merge_commmit2}" != "true" ]; then
    # マージコミット以外の場合
    log.debug_console "git revert --no-edit ${_commit_hash}"
    log.add_indent
    git revert --no-edit ${_commit_hash}                                                      2>&1 |
    log.debug_console
    _ret_code=${PIPESTATUS[0]}
    log.remove_indent

  elif [ "${_is_merge_commmit}" = "true" -a "${_is_merge_commmit2}" != "true" ]; then
    # マージコミットで、親1を残す場合
    log.debug_console "git revert -m 1 --no-edit ${_commit_hash}"
    log.add_indent
    git revert -m 1 --no-edit ${_commit_hash}                                                 2>&1 |
    log.debug_console
    _ret_code=${PIPESTATUS[0]}
    log.remove_indent

  else
    # マージコミットで、親2を残す場合
    log.debug_console "git revert -m 2 --no-edit ${_commit_hash}"
    log.add_indent
    git revert -m 2 --no-edit ${_commit_hash}                                                 2>&1 |
    log.debug_console
    _ret_code=${PIPESTATUS[0]}
    log.remove_indent
  fi

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "コミットハッシュ：${_commit_hash}の打ち消しに失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   pushを実行してローカルリポジトリの変更をリモートリポジトリに反映します。
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2  : ブランチ            ※任意
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.push() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR [TARGET_BRANCH]"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -lt 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ブランチ ※Git作業ディレクトリに移動に設定
  local _branch=

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # 事前にフェッチ実行
  git.fecth "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "フェッチに失敗しました。Git作業ディレクトリ：${_work_dir}、ブランチ：$2、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # ブランチ設定
  if [ "$2" = "" ]; then
    # 引数でブランチが指定されてない場合 ※カレントブランチを設定
    log.debug_console "git rev-parse --abbrev-ref HEAD"
    log.add_indent
    _branch=`git rev-parse --abbrev-ref HEAD`
    _ret_code=$?
    log.debug_console "${_branch}"
    log.remove_indent

    # 実行結果チェック
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.error_console "カレントブランチの取得に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi
  else
    # 引数でブランチが指定されている場合 ※引数のブランチを設定
    _branch=$2
  fi

  # rebase
  log.debug_console "git pull --rebase origin ${_branch}"
  log.add_indent
  git pull --rebase origin ${_branch}                                                         2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "rebase に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # push
  log.debug_console "git push origin ${_branch}:${_branch}"
  log.add_indent
  git push origin ${_branch}:${_branch}                                                       2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  # 実行結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "push に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   Git作業ディレクトリの編集内容を正にしたpush
#
# 引数
#   ・1  : git作業ディレクトリ
#   ・2  : コミットメッセージ
#
# 戻り値
#    0: 正常終了の場合
#    6: エラー発生時
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.staging_and_push() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR [TARGET_BRANCH]"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -lt 1 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # コミットメッセージ
  local _message="$2"

  # ステージング
  git.staging ${_work_dir}
  local _cur_ret_code=$?
  if [ ${_cur_ret_code} -eq ${EXITCODE_WARN} ]; then
    # ステージング対象が存在しない場合、スキップ
    log.debug_console "ステージング対象が存在しませんでした。"
    log.remove_indent
    return ${EXITCODE_SUCCESS}

  elif [ ${_cur_ret_code} -eq ${EXITCODE_ERROR} ]; then
    log.error_console "ステージングでエラーが発生しました。リターンコード：${_cur_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # コミット
  git.commit ${_work_dir} "${_message}"
  _cur_ret_code=$?
  if [ ${_cur_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "コミットでエラーが発生しました。リターンコード：${_cur_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # プッシュ
  git.push ${_work_dir}
  _cur_ret_code=$?
  if [ ${_cur_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "プッシュでエラーが発生しました。リターンコード：${_cur_ret_code}"
    cd - > /dev/null 2>&1
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   Fromタグ・Toタグの差分コミット一覧を出力します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: Fromタグ
#   ・3: Toタグ
#
# オプション
#   --merges: マージコミットのみ出力オプション
#       マージコミットのみ差分出力します。
#
#   --no-merges: マージコミット除外オプション
#       マージコミットを除外して差分出力します。
#
# 戻り値
#    0: 差分コミット一覧を取得できた場合
#    3: 差分コミット一覧が0件の場合
#    6: エラー発生時
#
# 標準出力
#   ・レイアウト
#     コミット日時,ハッシュ,ユーザ,コメント
#
#--------------------------------------------------------------------------------------------------
function git.diff_commit_list() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} [--merges|--no-merges] GIT_WORK_DIR FROM_TAG TO_TAG"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  local _option_merges=""

  while :; do
    case $1 in
      --merges)
        if [ "${_option_merges}" != "" ]; then
          log.error_log "--merges | --no-merges が両方指定されています。どちらか一方のみ指定して下さい。"
          return ${EXITCODE_ERROR}
        fi
        _option_merges=$1
        shift
        break
        ;;
      --no-merges)
        if [ "${_option_merges}" != "" ]; then
          log.error_log "--merges | --no-merges が両方指定されています。どちらか一方のみ指定して下さい。"
          return ${EXITCODE_ERROR}
        fi
        _option_merges=$1
        shift
        break
        ;;
      --)
        shift
        break
        ;;
      -*)
        log.error_console "${_USAGE}"
        exit ${EXITCODE_ERROR}
        ;;
      *)
        break
        ;;
    esac
  done

  # 引数の数
  if [ $# -ne 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Fromタグ
  local _tag_from="$2"

  # Toタグ
  local _tag_to="$3"

  # 一時作業ディレクトリ
  local _dirname_tmp=${FUNCNAME[0]}_$$
  local _dir_tmp=/tmp/${_dirname_tmp}
  mkdir -p ${_dir_tmp}

  # 一時ファイル名
  local _filename_commit_list=diff_commit_list
  local _file_commit_list=${_dir_tmp}/${_filename_commit_list}

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # 差分コミット一覧取得
  log.debug_console "git log ${_option_merges} --date=iso --pretty=format:'%cd,%H,%ce,%s' ${_tag_from}..${_tag_to}"
  log.add_indent
  git log ${_option_merges} --date=iso --pretty=format:'%cd,%H,%ce,%s' ${_tag_from}..${_tag_to}    |
  tee                                                                                                > ${_file_commit_list}
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  # 取得結果チェック
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "差分コミット一覧の取得に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    rm -rf ${_dir_tmp}
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  local _exit_code=${EXITCODE_SUCCESS}
  # 結果判定
  if [ -s ${_file_commit_list} ]; then
    # 差分コミット有りの場合
    _exit_code=${EXITCODE_SUCCESS}
    cat ${_file_commit_list}
  else
    # 0件の場合
    _exit_code=${EXITCODE_WARN}
  fi

  # 一時作業ディレクトリ削除
  log.debug_console "rm -rf ${_dir_tmp}"
  rm -rf ${_dir_tmp}

  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${_exit_code}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   git.diff_file_list を コミットモードで呼出します
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: Fromタグ
#   ・3: Toタグ
#
# オプション
#   -v: 詳細表示オプション
#       Fromタグ・Toタグ間に含まれる各コミットごとの差分ファイル一覧を取得します。
#
#   --merges: マージコミットのみ出力オプション
#       マージコミットのみ差分出力します。
#
#   --no-merges: マージコミット除外オプション
#       マージコミットを除外して差分出力します。
#
# 戻り値
#    git.diff_file_list を参照
#
#
#--------------------------------------------------------------------------------------------------
function git.commit_diff_file_list() {
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  git.diff_file_list "$@" "commit"
  local _ret_code=$?

  log.remove_indent
  return ${_ret_code}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   git.diff_file_list を ステータスモードで呼出します
#
# 引数
#   ・1: Git作業ディレクトリ
#
# オプション
#   -v: 詳細表示オプション
#       Fromタグ・Toタグ間に含まれる各コミットごとの差分ファイル一覧を取得します。
#
#   --merges: マージコミットのみ出力オプション
#       マージコミットのみ差分出力します。
#
#   --no-merges: マージコミット除外オプション
#       マージコミットを除外して差分出力します。
#
# 戻り値
#    git.diff_file_list を参照
#
#
#--------------------------------------------------------------------------------------------------
function git.status_diff_file_list() {
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  git.diff_file_list "$@" "dummy" "dummy" "status"
  local _ret_code=$?

  log.remove_indent
  return ${_ret_code}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   Fromタグ・Toタグの差分ファイル一覧を出力します。
#   モードによって、処理方法が異なります。
#
#     ・commitモード：git.diff_commit_listより差分コミットリストを取得、
#                     取得したコミットに含まれる差分ファイルを出力します。
#     ・statusモード：git status コマンドで差分のあるファイルを出力します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: Fromタグ            ※ statusモードの場合はダミー値を指定して下さい
#   ・3: Toタグ              ※ statusモードの場合はダミー値を指定して下さい
#   ・4: モード              ※ commit or status を指定して下さい
#
# オプション
#   -v: 詳細表示オプション
#       Fromタグ・Toタグ間に含まれる各コミットごとの差分ファイル一覧を取得します。
#
#   --merges: マージコミットのみ出力オプション
#       マージコミットのみ差分出力します。
#
#   --no-merges: マージコミット除外オプション
#       マージコミットを除外して差分出力します。
#
# 戻り値
#    0: 差分ファイル一覧を取得できた場合
#    3: 差分ファイル一覧が0件の場合
#    6: エラー発生時
#
# 標準出力
#   ・レイアウト-commitモード
#     コミット日時,ハッシュ,ユーザ,コメント,更新タイプ,ファイルパス
#
#   ・レイアウト-stautsモード
#     -,-,-,-,更新タイプ,ファイルパス
#
#--------------------------------------------------------------------------------------------------
function git.diff_file_list() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} [-v] GIT_WORK_DIR FROM_TAG TO_TAG MODE"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  local _is_detail=false
  local _option_merges=""

  # オプション解析
  while :; do
    case $1 in
      -v)
        _is_detail=true
        shift
        ;;
      --merges)
        if [ "${_option_merges}" != "" ]; then
          log.error_log "--merges | --no-merges が両方指定されています。どちらか一方のみ指定して下さい。"
          return ${EXITCODE_ERROR}
        fi
        _option_merges=$1
        shift
        break
        ;;
      --no-merges)
        if [ "${_option_merges}" != "" ]; then
          log.error_log "--merges | --no-merges が両方指定されています。どちらか一方のみ指定して下さい。"
          return ${EXITCODE_ERROR}
        fi
        _option_merges=$1
        shift
        break
        ;;
      --)
        shift
        break
        ;;
      -*)
        log.error_console "${_USAGE}"
        log.remove_indent
        return ${EXITCODE_ERROR}
        ;;
      *)
        break
        ;;
    esac
  done

  # 引数の数
  if [ $# -ne 4 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Fromタグ
  local _tag_from="$2"

  # Toタグ
  local _tag_to="$3"

  # モード
  local _mode="$4"
  if [ "${_mode}" != "commit" -a "${_mode}" != "status" ]; then
    log.error_console "モードには commit もしくは status を指定して下さい。モード：${_mode}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 一時作業ディレクトリ
  local _dirname_tmp=${FUNCNAME[0]}_$$
  local _dir_tmp=/tmp/${_dirname_tmp}
  mkdir -p ${_dir_tmp}

  # 一時差分コミットリストファイル名
  local _filename_commit_list=diff_commit_list
  local _file_commit_list=${_dir_tmp}/${_filename_commit_list}

  # 差分ファイルリストファイル名
  local _filename_file_list=diff_file_list
  local _file_file_list=${_dir_tmp}/${_filename_file_list}

  # スキップ対象行頭文字列
  local _skiprow_head_values=()
  _skiprow_head_values+=( commit )
  _skiprow_head_values+=( Merge )
  _skiprow_head_values+=( Author )
  _skiprow_head_values+=( Date )

  # 更新タイプ
  local _file_status_rename=R

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # ファイルループ用に、区切り文字を一時的に 改行コード のみに変更
  local _before_ifs=$IFS
  IFS=$'\n'

  if [ "${_mode}" = "commit" ]; then
    # コミットモードの場合

    # Git作業ディレクトリの最新化
    git.reset ${_work_dir} >> ${PATH_LOG} 2>&1
    _ret_code=${PIPESTATUS[0]}

    # 実行結果チェック
    if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
      # エラーの場合
      log.error_console "Git作業ディレクトリの最新化に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
      rm -rf ${_dir_tmp}
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

    # 差分コミット一覧取得
    git.diff_commit_list ${_option_merges} ${_work_dir} ${_tag_from} ${_tag_to}                      > ${_file_commit_list}
    _ret_code=$?

    # 取得結果チェック
    if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
      # 警告の場合 ※差分無し
      rm -rf ${_dir_tmp}
      log.remove_indent
      return ${EXITCODE_WARN}

    elif [ ${_ret_code} -eq ${EXITCODE_ERROR} ]; then
      # エラーの場合
      log.error_console "差分コミット一覧の取得に失敗しました。リターンコード：${_ret_code}"
      rm -rf ${_dir_tmp}
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

    log.debug_console "cat ${_file_commit_list}"
    log.add_indent
    for _cur_comitlist_line in `cat ${_file_commit_list}`; do
      # コミット日時
      local _cur_commit_date=`echo ${_cur_comitlist_line} | cut -d ',' -f 1`
      # コミットハッシュ
      local _cur_commit_hash=`echo ${_cur_comitlist_line} | cut -d ',' -f 2`
      # コミットユーザ
      local _cur_commit_user=`echo ${_cur_comitlist_line} | cut -d ',' -f 3`
      # コミットコメント
      local _cur_commit_comment=`echo ${_cur_comitlist_line} | cut -d ',' -f 4`

      #------------------------------------------------------------------------------
      # 現ループ対象コミットでの差分ファイル一覧取得
      #
      # レイアウト
      #   commit ,コミットハッシュ
      #   Merge: Fromコミットハッシュ Toコミットハッシュ ※ Merge時のみ出力あり
      #   Author: authorの名前
      #   Date:   コミット日付
      #
      #       コミットコメント
      #
      #   A ファイルパス1
      #   D ファイルパス2
      #   RXXX ファイルパス3_OLD ファイルパス3_NEW   ※リネーム or ムーブ XXXには番号が入る
      #   M ファイルパス4
      #
      #------------------------------------------------------------------------------
      # 一時ファイル名
      local _cur_filename_filelist=diff_file_list_${_cur_commit_hash}
      local _cur_file_filelist=${_dir_tmp}/${_cur_filename_filelist}

      log.debug_console "git log --name-status ${_cur_commit_hash} -1"
      git log --name-status ${_cur_commit_hash} -1                                                 |
      tee                                                                                            > ${_cur_file_filelist}
      _ret_code=${PIPESTATUS[0]}                                                                     # パイプの1つめの戻り値を取得

      # 取得結果チェック
      if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
        log.error_console "コミットハッシュ：${_cur_commit_hash}の差分ファイル一覧の取得に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
        rm -rf ${_dir_tmp}
        log.remove_indent
        log.remove_indent
        return ${EXITCODE_ERROR}
      fi

      #--------------------------------------------------------------------------------
      # 差分ファイル一覧を全件ループ
      #--------------------------------------------------------------------------------
      log.debug_console "cat ${_cur_file_filelist} | grep -v '^\s*#'"
      log.add_indent
      for _cur_filelist_line in `cat ${_cur_file_filelist} | grep -v '^\s*#'`; do
        # スキップ対象(ヘッダ)かチェック
        local _is_skip_line=false
        for _cur_skip_value in ${_skiprow_head_values[@]}; do
          if [ "`echo ${_cur_filelist_line} | grep ^${_cur_skip_value}`" != "" ]; then
            # 行頭が読み飛ばし対象の文字列の場合
            _is_skip_line=true
            break
          fi
        done

        # スキップ対象(ヘッダ)を読み飛ばす
        if [ "${_is_skip_line}" = "true" ]; then
          continue
        fi

        # コミットコメント行を読み飛ばす
        if [ "${_cur_filelist_line:0:1}" = " " ]; then
          continue
        fi

        # ステータス
        local _cur_file_status=${_cur_filelist_line:0:1}

        # ファイルパス
        if [ ${_cur_file_status} != ${_file_status_rename} ]; then
          # 追加(A)・更新(M)・削除(D)の場合
          local _cur_file_path=`echo ${_cur_filelist_line} | cut -d $'\t' -f 2`
        else
          # リネーム or 移動(R)の場合 ※ NEWのファイルパスを取得
          local _cur_file_path=`echo ${_cur_filelist_line} | cut -d $'\t' -f 3`
        fi

        # 出力
        echo -n  "${_cur_commit_date}"                                                               >> ${_file_file_list}
        echo -n ",${_cur_commit_hash}"                                                               >> ${_file_file_list}
        echo -n ",${_cur_commit_user}"                                                               >> ${_file_file_list}
        echo -n ",${_cur_commit_comment}"                                                            >> ${_file_file_list}
        echo -n ",${_cur_file_status}"                                                               >> ${_file_file_list}
        echo    ",${_cur_file_path}"                                                                 >> ${_file_file_list}
      done
      log.remove_indent

    done
    log.remove_indent

  else
    # ステータスモードの場合

    # 一時ファイル名
    local _filename_filelist=status_diff_file_list
    local _file_filelist=${_dir_tmp}/${_filename_filelist}

    log.debug_console "git status -s -uall"
    git status -s -uall                                                                            |
    tee                                                                                              > ${_file_filelist}
    _ret_code=${PIPESTATUS[0]}                                                                       # パイプの1つめの戻り値を取得

    # 取得結果チェック
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.error_console "ステータスの取得に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
      rm -rf ${_dir_tmp}
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

    # 差分ファイルが存在するかチェック
    if [ ! -s ${_file_filelist} ]; then
      # 存在しない場合
      rm -rf ${_dir_tmp}
      log.remove_indent
      return ${EXITCODE_WARN}
    fi

    # git status の結果をループ
    log.debug_console "cat ${_file_filelist} | sed -e \"s|^  *||g\" | sed -e \"s|  *$||g\""
    log.add_indent
    for _cur_line in `cat ${_file_filelist} | sed -e "s|^  *||g" | sed -e "s|  *$||g"`; do
      # スタータス ※ "??"（untracked）は A （追加）として置き換え
      local _cur_file_status=`echo ${_cur_line} | cut -d ' ' -f 1 | sed 's|??|A|g'`
      # ファイルパス
      local _cur_file_path=`echo ${_cur_line} | cut -d ' ' -f 2`

      # 出力
      echo -n  "-"                                                                                   >> ${_file_file_list}
      echo -n ",-"                                                                                   >> ${_file_file_list}
      echo -n ",-"                                                                                   >> ${_file_file_list}
      echo -n ",-"                                                                                   >> ${_file_file_list}
      echo -n ",${_cur_file_status}"                                                                 >> ${_file_file_list}
      echo    ",${_cur_file_path}"                                                                   >> ${_file_file_list}

    done
    log.remove_indent

  fi

  # 区切り文字を戻す
  IFS=${_before_ifs}

  # 最新情報にサマリー
  if [ "${_is_detail}" != "true" ]; then
    # 詳細出力ではない場合

    nl -s ',' ${_file_file_list}                                                                   | # コミット日時でソートすると順番が崩れるため、行番号を付与 ※秒がないため
#    tee > ${_file_file_list}.tmp
#    cat ${_file_file_list}.tmp                                                                     |
    sort -t ',' -k 7 -u                                                                            | # 各ファイル毎に最新情報のみに絞り込み
#    tee > ${_file_file_list}.tmp.tmp
#    cat ${_file_file_list}.tmp.tmp                                                                 | # テスト用にファイル出力
    sort                                                                                           | # 行番号順に再度ソート
    awk 'BEGIN {FS=","; OFS=","} {print $2,$3,$4,$5,$6,$7}'                                        | # 行番号をカットして出力
    tee > ${_file_file_list}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  local _exit_code=${EXITCODE_SUCCESS}
  # 結果判定
  if [ -s ${_file_file_list} ]; then
    # 差分コミット有りの場合
    _exit_code=${EXITCODE_SUCCESS}
    cat ${_file_file_list}
  else
    # 0件の場合
    _exit_code=${EXITCODE_WARN}
  fi

  # 一時作業ディレクトリ削除
  log.debug_console "rm -rf ${_dir_tmp}"
  rm -rf ${_dir_tmp}

  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${_exit_code}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   git.diff_file_details を コミットモードで呼出します
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: Fromタグ
#   ・3: Toタグ
#
# オプション
#   -v: 詳細表示オプション
#       Fromタグ・Toタグ間に含まれる各コミットごとの差分ファイル一覧を取得します。
#
#   --merges: マージコミットのみ出力オプション
#       マージコミットのみ差分出力します。
#
#   --no-merges: マージコミット除外オプション
#       マージコミットを除外して差分出力します。
#
# 戻り値
#    git.diff_file_list を参照
#
#
#--------------------------------------------------------------------------------------------------
function git.commit_diff_file_details() {
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  git.diff_file_details "$@" "commit"
  local _ret_code=$?

  log.remove_indent
  return ${_ret_code}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   git.diff_file_details を ステータスモードで呼出します
#
# 引数
#   ・1: Git作業ディレクトリ
#
# オプション
#   -v: 詳細表示オプション
#       Fromタグ・Toタグ間に含まれる各コミットごとの差分ファイル一覧を取得します。
#
#   --merges: マージコミットのみ出力オプション
#       マージコミットのみ差分出力します。
#
#   --no-merges: マージコミット除外オプション
#       マージコミットを除外して差分出力します。
#
# 戻り値
#    git.diff_file_list を参照
#
#
#--------------------------------------------------------------------------------------------------
function git.status_diff_file_details() {
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  git.diff_file_details "$@" "HEAD" "WORK" "status"
  local _ret_code=$?

  log.remove_indent
  return ${_ret_code}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   Fromタグ・Toタグの差分ファイル詳細を出力します。
#   モードによって、処理対象の差分ファイルリストが異なります。
#
#     ・commitモード：git.diff_file_list を commit モードで実行した結果の差分ファイルリストを対象にする。
#     ・statusモード：git.diff_file_list を status モードで実行した結果の差分ファイルリストを対象にする。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: Fromタグ            ※ statusモードの場合はダミー値を指定して下さい
#   ・3: Toタグ              ※ statusモードの場合はダミー値を指定して下さい
#   ・4: モード              ※ commit or status を指定して下さい
#
# オプション
#   -v: 詳細表示オプション
#       Fromタグ・Toタグ間に含まれる各コミットごとの差分ファイル詳細を取得します。
#
#   --merges: マージコミットのみ出力オプション
#       マージコミットのみ差分出力します。
#
#   --no-merges: マージコミット除外オプション
#       マージコミットを除外して差分出力します。
#
# 戻り値
#    0: 差分ファイル詳細を取得できた場合
#    3: 差分ファイル詳細に出力する内容が無い場合
#    6: エラー発生時
#
#--------------------------------------------------------------------------------------------------
function git.diff_file_details() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} [-v] GIT_WORK_DIR FROM_TAG TO_TAG MODE"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  local _is_detail=false
  local _option_merges=""

  # オプション解析
  while :; do
    case $1 in
      -v)
        _is_detail=true
        shift
        ;;
      --merges)
        if [ "${_option_merges}" != "" ]; then
          log.error_log "--merges | --no-merges が両方指定されています。どちらか一方のみ指定して下さい。"
          return ${EXITCODE_ERROR}
        fi
        _option_merges=$1
        shift
        break
        ;;
      --no-merges)
        if [ "${_option_merges}" != "" ]; then
          log.error_log "--merges | --no-merges が両方指定されています。どちらか一方のみ指定して下さい。"
          return ${EXITCODE_ERROR}
        fi
        _option_merges=$1
        shift
        break
        ;;
      --)
        shift
        break
        ;;
      -*)
        log.error_console "${_USAGE}"
        log.remove_indent
        return ${EXITCODE_ERROR}
        ;;
      *)
        break
        ;;
    esac
  done

  # 引数の数
  if [ $# -ne 4 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Fromタグ
  local _tag_from="$2"

  # Toタグ
  local _tag_to="$3"

  # モード
  local _mode="$4"
  if [ "${_mode}" != "commit" -a "${_mode}" != "status" ]; then
    log.error_console "モードには commit もしくは status を指定して下さい。モード：${_mode}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 元ディレクトリ
  local _before_dir=`pwd`

  # 一時作業ディレクトリ
  local _dirname_tmp=${FUNCNAME[0]}_$$
  local _dir_tmp=/tmp/${_dirname_tmp}
  mkdir -p ${_dir_tmp}

  # 一時zip展開ディレクトリ
  local _dirname_zip=zip
  local _dir_zip_root=${_dir_tmp}/zip
  mkdir -p ${_dir_zip_root}

  # 一時差分ファイルリストファイル名
  local _filename_file_list=diff_file_list
  local _file_file_list=${_dir_tmp}/${_filename_file_list}

  # 差分ファイル詳細ファイル名
  local _filename_file_details=diff_file_details
  local _file_file_details=${_dir_tmp}/${_filename_file_details}

  # 更新タイプ
  local _file_status_add=A
  local _file_status_modify=M
  local _file_status_rename=R
  local _file_status_delete=D

  # zip差分を取得する拡張子
  local _zip_extensions=()
  _zip_extensions+=( jar )
  _zip_extensions+=( war )
  _zip_extensions+=( ear )

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # 差分ファイル一覧取得
  if [ "${_is_detail}" != "true" ]; then
    # From・To差分の場合
    git.diff_file_list ${_option_merges} ${_work_dir} ${_tag_from} ${_tag_to} ${_mode}               > ${_file_file_list}
  else
    # 詳細表示の場合
    git.diff_file_list -v ${_option_merges} ${_work_dir} ${_tag_from} ${_tag_to} ${_mode}            > ${_file_file_list}
  fi

  _ret_code=$?

  # 取得結果チェック
  if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
    # 警告の場合 ※差分無し
    rm -rf ${_dir_tmp}
    log.remove_indent
    return ${EXITCODE_WARN}

  elif [ ${_ret_code} -eq ${EXITCODE_ERROR} ]; then
    # エラーの場合
    log.error_console "差分ファイル一覧の取得に失敗しました。リターンコード：${_ret_code}"
    rm -rf ${_dir_tmp}
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # From・To情報出力
  if [ "${_is_detail}" != "true" ]; then
    # From・To差分の場合
    echo "diff FROM:${_tag_from} - TO:${_tag_to}"                                                    >> ${_file_file_details}
    echo ""                                                                                          >> ${_file_file_details}
  fi

  local _before_commit_hash=""

  # ファイルループ用に、区切り文字を一時的に 改行コード のみに変更
  local _before_ifs=$IFS
  IFS=$'\n'

  log.debug_console "cat ${_file_file_list}"
  log.add_indent
  for _cur_filelist_line in `cat ${_file_file_list}`; do
    # コミット日時
    local _cur_commit_date=`echo ${_cur_filelist_line} | cut -d ',' -f 1`
    # コミットハッシュ
    local _cur_commit_hash=`echo ${_cur_filelist_line} | cut -d ',' -f 2`
    # コミットユーザ
    local _cur_commit_user=`echo ${_cur_filelist_line} | cut -d ',' -f 3`
    # コミットコメント
    local _cur_commit_comment=`echo ${_cur_filelist_line} | cut -d ',' -f 4`
    # 更新タイプ
    local _cur_file_status=`echo ${_cur_filelist_line} | cut -d ',' -f 5`
    # ファイルパス
    local _cur_file_path=`echo ${_cur_filelist_line} | cut -d ',' -f 6`
    # ファイル名
    local _cur_file_name=`basename ${_cur_file_path}`

    # zipファイルチェック
    _is_zip_file=false
    for _cur_zip_extension in ${_zip_extensions[@]}; do
      if [ "${_cur_file_name##*.}" = "${_cur_zip_extension}" ]; then
        # 対象の拡張子の場合
        _is_zip_file=true
        break
      fi
    done

    #------------------------------------------------------------------------------
    # ヘッダ情報出力
    #------------------------------------------------------------------------------
    if [ "${_is_detail}" != "true" -o "${_mode}" != "commit" ]; then
      # From・To差分 もしくは ステータスモードの場合 ※ 区切り行 + 差分ファイルリストの行を出力

      echo "--------------------------------------------------"                                      >> ${_file_file_details}
      echo "${_cur_filelist_line}"                                                                   >> ${_file_file_details}

      # 差分取得用のタグを設定
      local _tag_old=""
      local _tag_new=""
      if [ "${_mode}" = "commit" ]; then
        # コミットモードの場合
        _tag_old=${_tag_from}
        _tag_new=${_tag_to}
      else
        # ステータスモードの場合
        _tag_old=HEAD
        _tag_new=dummy
      fi

    else
      # 詳細表示 かつ コミットモードの場合 ※ git show コマンドの結果を出力

      if [ "${_cur_commit_hash}" != "${_before_commit_hash}" ]; then
        # ループ対象のコミットが変わった場合
        local _cur_filename_flat=flat_${_cur_commit_hash}
        local _cur_file_flat=${_dir_tmp}/${_cur_filename_flat}

        log.debug_console "git show ${_cur_commit_hash}"
        git show ${_cur_commit_hash}                                                                 > ${_cur_file_flat}
        _ret_code=${PIPESTATUS[0]}                                                                   # パイプの1つめの戻り値を取得

        # 実行結果チェック
        if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
          # エラーの場合
          log.error_console "コミットハッシュ：${_cur_commit_hash}の詳細情報の取得に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
          rm -rf ${_dir_tmp}
          log.remove_indent
          log.remove_indent
          return ${EXITCODE_ERROR}
        fi

        # 出力
        cat ${_cur_file_flat}                                                                        >> ${_file_file_details}
        echo -e "\n"                                                                                 >> ${_file_file_details}

      fi

      # 差分情報出力するかチェック ※ zipファイル以外の場合はスキップ(git show の結果に差分が出力されているため)
      if [ "${_is_zip_file}" != "true" ]; then
        # zipファイル以外の場合
        _before_commit_hash=${_cur_commit_hash}
        continue
      fi

      # 差分取得用のタグを設定
      local _tag_old=""
      local _tag_new=""
      if [ "${_mode}" = "commit" ]; then
        # コミットモードの場合
        _tag_old=${_cur_commit_hash}^
        _tag_new=${_cur_commit_hash}
      else
        # ステータスモードの場合
        _tag_old=HEAD
        _tag_new=dummy
      fi

      # ファイルパス出力
      echo "contents diff ${_cur_file_path}"                                                         >> ${_file_file_details}

    fi

    #------------------------------------------------------------------------------
    # 差分情報出力
    #------------------------------------------------------------------------------
    if [ "${_cur_file_status}" = "${_file_status_add}" -o "${_cur_file_status}" = "${_file_status_modify}" ]; then
      # 追加・更新の場合

      if [ "${_is_zip_file}" = "true" ]; then
        # zipファイルの場合
        #------------------------------------------------------------------------------
        # zip展開ディレクトリ作成
        #------------------------------------------------------------------------------
        _cur_filename_zip_diff=zip_diff_`echo ${_tag_old} | sed "s|/|-|g"`_`echo ${_tag_new} | sed "s|/|-|g"`_${_cur_file_name}.txt
        _cur_file_zip_diff=${_dir_tmp}/${_cur_filename_zip_diff}
        _cur_dir_zip_old=${_dir_zip_root}/`echo ${_tag_old} | sed "s|/|-|g"`/${_cur_file_name}
        _cur_dir_zip_new=${_dir_zip_root}/`echo ${_tag_new} | sed "s|/|-|g"`/${_cur_file_name}
        mkdir -p ${_cur_dir_zip_old}
        mkdir -p ${_cur_dir_zip_new}

        #------------------------------------------------------------------------------
        # oldファイル展開 ※ ${_tag_old}時点のファイルをチェックアウトしてzip展開
        #------------------------------------------------------------------------------
        if [ "${_cur_file_status}" = "${_file_status_modify}" ]; then
          # 更新の場合 ※ 追加の場合は存在しないのでスキップ

          # ${_tag_old}時点のファイル存在チェック
          if [ "`git ls-tree -r ${_tag_old} ${_cur_file_path}`" != "" ]; then
            # ${_tag_old}時点にファイルが存在する場合

            # ステータスモードかチェック
            if [ "${_mode}" = "status" ]; then
              # ステータスモードの場合 ※ ${_tag_old}時点のファイルチェックアウト後にファイルを復元するため退避
              cp -rf ${_cur_file_path} ${_dir_tmp}/${_cur_file_name}
            fi

            # ファイル取得
            log.debug_console "git checkout ${_tag_old} ${_cur_file_path}"
            log.add_indent
            git checkout ${_tag_old} ${_cur_file_path}                                        2>&1 |
            log.debug_console
            _ret_code=${PIPESTATUS[0]}
            log.remove_indent

            # 実行結果チェック
            if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
              # エラーの場合
              log.error_console "ファイルのチェックアウトに失敗しました。Git作業ディレクトリ：${_work_dir}、コミットハッシュ：${_tag_new}、ファイルパス：${_cur_file_path}、リターンコード：${_ret_code}"
              rm -rf ${_dir_tmp}
              log.remove_indent
              log.remove_indent
              return ${EXITCODE_ERROR}
            fi

            # oldファイルをzip展開ディレクトリにコピー
            log.debug_console "cp -rf ${_cur_file_path} ${_cur_dir_zip_old}/${_cur_file_name}"
            cp -rf ${_cur_file_path} ${_cur_dir_zip_old}/${_cur_file_name}

            # ステータスモードかチェック
            if [ "${_mode}" = "status" ]; then
              # ステータスモードの場合 ※ ファイルを復元
              cp -rf ${_dir_tmp}/${_cur_file_name} ${_cur_file_path}
            fi

            # zipファイルの展開
            log.debug_console "cd ${_cur_dir_zip_old}"
            cd ${_cur_dir_zip_old}

            log.debug_console "jar xvf ${_cur_file_name}"
            jar xvf ${_cur_file_name} > /dev/null 2>&1
            _ret_code=$?

            # 解凍結果チェック
            if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
              log.error_console "zipファイルを展開できませんでした。zipファイル：${_cur_dir_zip_old}/${_cur_file_name}、リターンコード：${_ret_code}"
              rm -rf ${_dir_tmp}
              log.remove_indent
              log.remove_indent
              return ${EXITCODE_ERROR}
            fi

            # 解凍処理前のディレクトリに戻る
            log.debug_console "cd -"
            cd - > /dev/null 2>&1

            # zipファイル削除 ※差分取得時のリストに含まれてしまうため
            log.debug_console "rm -fr  ${_cur_dir_zip_old}/${_cur_file_name}"
            rm -fr  ${_cur_dir_zip_old}/${_cur_file_name}
          fi

        fi

        #------------------------------------------------------------------------------
        # newファイル展開  ※ ${_tag_new}時点のファイルをチェックアウトしてzip展開
        #------------------------------------------------------------------------------
        # newファイル取得
        if [ "${_mode}" = "commit" ]; then
          # コミットモードの場合 ※ ステータスモード場合はワークツリーのファイルが最新なのでスキップ
          log.debug_console "git checkout ${_tag_new} ${_cur_file_path}"
          log.add_indent
          git checkout ${_tag_new} ${_cur_file_path}                                          2>&1 |
          log.debug_console
          _ret_code=${PIPESTATUS[0]}
          log.remove_indent

          # 実行結果チェック
          if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
            # エラーの場合
            log.error_console "ファイルのチェックアウトに失敗しました。Git作業ディレクトリ：${_work_dir}、コミットハッシュ：${_tag_new}、ファイルパス：${_cur_file_path}、リターンコード：${_ret_code}"
            rm -rf ${_dir_tmp}
            log.remove_indent 2
            return ${EXITCODE_ERROR}
          fi
        fi

        # newファイルをzip展開ディレクトリにコピー
        log.debug_console "cp -rf ${_cur_file_path} ${_cur_dir_zip_new}/${_cur_file_name}"
        cp -rf ${_cur_file_path} ${_cur_dir_zip_new}/${_cur_file_name}

        # zipファイルの展開
        log.debug_console "cd ${_cur_dir_zip_new}"
        cd ${_cur_dir_zip_new}

        log.debug_console "jar xvf ${_cur_file_name}"
        jar xvf ${_cur_file_name} > /dev/null 2>&1
        _ret_code=$?

        # 解凍結果チェック
        if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
          log.error_console "zipファイルを展開できませんでした。zipファイル：${_cur_dir_zip_new}/${_cur_file_name}、リターンコード：${_ret_code}"
          rm -rf ${_dir_tmp}
          log.remove_indent
          log.remove_indent
          return ${EXITCODE_ERROR}
        fi

        # 解凍処理前のディレクトリに戻る
        log.debug_console "cd -"
        cd - > /dev/null 2>&1

        # zipファイル削除 ※差分取得時のリストに含まれてしまうため
        log.debug_console "rm -fr ${_cur_dir_zip_new}/${_cur_file_name}"
        rm -fr ${_cur_dir_zip_new}/${_cur_file_name}

        #------------------------------------------------------------------------------
        # 差分取得
        #------------------------------------------------------------------------------
        log.debug_console "${DIR_BIN_LIB}/dir_diff.sh ${_cur_dir_zip_old} ${_cur_dir_zip_new}"
        ${DIR_BIN_LIB}/dir_diff.sh ${_cur_dir_zip_old} ${_cur_dir_zip_new}                           > ${_cur_file_zip_diff}
        _ret_code=$?

        # 結果チェック
        if [ "${_ret_code}" = "${EXITCODE_WARN}" ]; then
          # 差分有りの場合
          cat ${_cur_file_zip_diff}                                                                  >> ${_file_file_details}
          echo ""                                                                                    >> ${_file_file_details}
        elif [ "${_ret_code}" = "${EXITCODE_ERROR}" ]; then
          # 実行エラーの場合
          log.error_console "zipファイル差分取得処理時にエラーが発生しました。リターンコード：${_ret_code}"
          rm -rf ${_dir_tmp}
          log.remove_indent
          log.remove_indent
          return ${EXITCODE_ERROR}
        fi

      else
        # zipファイル以外の場合

        if [ "${_cur_file_status}" = "${_file_status_add}" ]; then
          # 追加の場合 ※cat結果を出力

          # newファイル取得
          if [ "${_mode}" = "commit" ]; then
            # コミットモードの場合 ※ ステータスモード場合はワークツリーのファイルが最新なのでスキップ
            log.debug_console "git checkout ${_tag_new} ${_cur_file_path}"
            log.add_indent
            git checkout ${_tag_old} ${_cur_file_path}                                        2>&1 |
            log.debug_console
            _ret_code=${PIPESTATUS[0]}
            log.remove_indent

            # 実行結果チェック
            if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
              # エラーの場合
              log.error_console "ファイルのチェックアウトに失敗しました。Git作業ディレクトリ：${_work_dir}、コミットハッシュ：${_tag_new}、ファイルパス：${_cur_file_path}、リターンコード：${_ret_code}"
              rm -rf ${_dir_tmp}
              log.remove_indent 2
              return ${EXITCODE_ERROR}
            fi
          fi

          cat ${_cur_file_path}                                                                      >> ${_file_file_details}
          echo -e "\n"                                                                               >> ${_file_file_details}
        else
          # 更新の場合 ※git diff コマンドの結果で1行目以降を出力
          local _cur_filename_diff=diff_${_cur_file_name}
          local _cur_file_diff=${_dir_tmp}/${_cur_filename_diff}

          if [ "${_mode}" = "commit" ]; then
            # コミットモードの場合 ※ ${_tag_old}と${_tag_new}間の差分
            log.debug_console "git diff ${_tag_old}..${_tag_new} -- ${_cur_file_path}"
            git diff ${_tag_old}..${_tag_new} -- ${_cur_file_path}                                 |
            tee                                                                                      > ${_cur_file_diff}
          else
            # ステータスモードの場合 ※ ワークツリーとインデックス間の差分
            log.debug_console "git diff -- ${_cur_file_path}"
            git diff -- ${_cur_file_path}                                                          |
            tee                                                                                      > ${_cur_file_diff}
          fi

          _ret_code=${PIPESTATUS[0]}                                                                 # パイプの1つめの戻り値を取得

          # 実行結果チェック
          if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
            # エラーの場合
            log.error_console "差分が取得できませんでした。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
            rm -rf ${_dir_tmp}
            log.remove_indent
            log.remove_indent
            return ${EXITCODE_ERROR}
          fi

          # 出力
          cat ${_cur_file_diff}                                                                    |
          sed -e "1d"                                                                                >> ${_file_file_details}
          echo ""                                                                                    >> ${_file_file_details}
        fi

      fi

    elif [ "${_cur_file_status}" = "${_file_status_rename}" ]; then
      # リネーム・移動の場合
      echo "new file path: ${_cur_file_path}"                                                        >> ${_file_file_details}
      echo ""                                                                                        >> ${_file_file_details}
    elif [ "${_cur_file_status}" = "${_file_status_delete}" ]; then
      # 削除の場合
      echo "deleted"                                                                                 >> ${_file_file_details}
      echo ""                                                                                        >> ${_file_file_details}
    fi

    _before_commit_hash=${_cur_commit_hash}

  done
  log.remove_indent

  # 区切り文字を戻す
  IFS=${_before_ifs}

  #------------------------------------------------------------------------------------------------
  # 作業ディレクトリの変更破棄 ※ 最新(HEAD)の状態に戻す
  #------------------------------------------------------------------------------------------------
  if [ "${_mode}" = "commit" ]; then
    # コミットモードの場合 ※ ステータスモードの場合は未コミット分が削除されるのでスキップ

    git.reset ${_work_dir} >> ${PATH_LOG} 2>&1
    _ret_code=${PIPESTATUS[0]}

    # 実行結果チェック
    if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
      # エラーの場合
      log.error_console "作業ディレクトリのリセットに失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
      rm -rf ${_dir_tmp}
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  local _exit_code=${EXITCODE_SUCCESS}
  # 結果判定
  if [ -s ${_file_file_details} ]; then
    # 差分コミット有りの場合
    _exit_code=${EXITCODE_SUCCESS}
    cat ${_file_file_details}
  else
    # 0件の場合
    _exit_code=${EXITCODE_WARN}
  fi

  # 一時作業ディレクトリ削除
  log.debug_console "rm -rf ${_dir_tmp}"
  rm -rf ${_dir_tmp}

  # 元ディレクトリに移動
  log.debug_console "cd ${_before_dir}"
  cd ${_before_dir}

  log.remove_indent
  return ${_exit_code}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   共通のフォーマットを指定した git log コマンドを実行します。
#
# 引数
#   なし
#
# 出力
#   コマンド実行結果
#
#--------------------------------------------------------------------------------------------------
function git.log() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # フェッチ
  git.fecth `pwd` >> ${PATH_LOG} 2>&1
  if [ $? -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  # 作業ディレクトリ情報を表示
  log.info_console "Git作業ディレクトリ: `pwd`"
  log.info_console "カレントブランチ   : `git rev-parse --abbrev-ref HEAD`"

  # git log
#  git log --graph --all --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit --date=relative
  log.debug_console "git log --graph --all --pretty=format:'%h -%d %s (%cr) <%an>' --abbrev-commit --date=relative"
  log.add_indent
  git log --graph --all --pretty=format:'%h -%d %s (%cr) <%an>' --abbrev-commit --date=relative 2>&1 |
  log.info_console
  log.remove_indent

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   指定のパスにローカルリポジトリを作成します。
#
# 引数
#   ・$1: ローカルリポジトリ作成ディレクトリルート
#   ・$2: グループディレクトリ ※任意
#   ・$3: リポジトリ名
#   ・$4: プロジェクトパス
#
# リターンコード
#    0: 正常終了
#    6: エラー発生時
#
#--------------------------------------------------------------------------------------------------
function git.create_repository_local() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} [-g] LOCAL_REPOSITORY_DIR_ROOT [GROUP] REPOSITORY_NAME PROJECT_PATH"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # グローバルコンフィグ使用有無
  local _is_use_global_config=false

  # オプション解析
  while :; do
    case $1 in
      -g|--use-global-config)
        _is_use_global_config=true
        shift
        ;;
      --)
        shift
        break
        ;;
      -*)
        log.error_console "${_USAGE}"
        log.remove_indent
        return ${EXITCODE_ERROR}
        ;;
      *)
        break
        ;;
    esac
  done

  # 引数の数
  if [ $# -lt 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ローカルリポジトリ作成先ルート
  local _local_repository_dir_root="$1"
  if [ ! -d ${_local_repository_dir_root} ]; then
    # 存在しない場合 ※ディレクトリ作成
    log.debug_console "mkdir -p ${_local_repository_dir_root}"
    mkdir -p ${_local_repository_dir_root}
  fi

  # グループ
  local _group=""
  if [ $# -eq 4 ]; then
    # 引数4つ（グループ指定有り）の場合
    _group="$2"
    shift
  fi

  # リポジトリ名
  local _repository_name=$2

  # プロジェクトパス
  local _project_path=$3

  # ローカルリポジトリ作成先パス組み立て
  local _repository_path=""
  if [ -n "${_group}" ]; then
    # グループが指定されている場合
    _repository_path=${_local_repository_dir_root}/${_group}/${_repository_name}/${_project_path}
  else
    # グループが指定されてない場合
    _repository_path=${_local_repository_dir_root}/${_repository_name}/${_project_path}
  fi

  # ディレクトリ存在チェック
  if [ -d ${_repository_path} ]; then
    # 存在する場合
    if [ -d ${_repository_path}/.git ]; then
      # Git作業ディレクトリの場合
      log.error_console "ローカルリポジトリ作成先パスに、既にリポジトリが存在します。ローカルリポジトリ作成先パス：${_repository_path}"
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi
  else
    # 存在しない場合
    log.debug_console "mkdir -p ${_repository_path}"
    mkdir -p ${_repository_path}
  fi


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # ローカルリポジトリ作成先に移動
  log.debug_console "cd ${_repository_path}"
  cd "${_repository_path}"

  # README.mdファイル作成
  local _readme_file="README.md"
  echo "`basename ${_repository_path}` repository."                                                  > ./${_readme_file}

  # Gitリポジトリとして初期化
  log.debug_console "git init"
  log.add_indent
  git init                                                                                    2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "Gitリポジトリの初期化に失敗しました。リポジトリパス：${_repository_path}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # git.clone pluginを流用して設定を追加
  if [ "${_is_use_global_config}" != "true" ]; then
    # global config を使用しない場合 ※ リポジトリ毎に config を設定

    local _trg_dir=${DIR_PLUGIN}/common/git.clone
    export PATH_LOG
    log.debug_console "find ${_trg_dir} -maxdepth 1 -follow -type f -name \*.sh | sort"
    log.add_indent
    for _cur_file_path in `find ${_trg_dir} -maxdepth 1 -follow -type f -name \*.sh | sort`; do
      local _cur_file_name=`basename ${_cur_file_path}`
      local _cur_file_relpath=`echo ${_cur_file_path} | sed -e "s|${DIR_PLUGIN}/||"`

      # plugin実行
      log.debug_console "${_cur_file_path} ${_work_dir}"
      log.add_indent
      ${_cur_file_path} "${_repository_path}"
      _ret_code=$?
      log.remove_indent

      # 戻り値を確認
      if [ ${_ret_code} -eq ${EXITCODE_ERROR} ]; then
        log.error_console "${_cur_file_relpath} でエラーが発生しました。リターンコード：${_ret_code}"
        log.remove_indent
        log.remove_indent
        return ${EXITCODE_ERROR}
      fi

    done
  fi
  log.remove_indent

  # 初期コミット用にステージング
  git.staging "${_repository_path}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 初期コミット実施
  local _commit_comment="first commit."
  git.commit "${_repository_path}" "${_commit_comment}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   対象を指定してアーカイブを作成します。
#   ※ tar.gz 形式で出力します。
#
#   ・tar.gz解凍後のディレクトリ構成
#      - アーカイブ対象/  ※「アーカイブ対象」に含まれる "/ "は "-" に変換されます
#         - リポジトリで管理しているファイル群
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: アーカイブ対象 ※ タグ / ブランチ / コミットハッシュ
#   ・3: 出力先ディレクトリ
#
# 出力
#   出力先ディレクトリ/アーカイブ対象.tar.gz
#
#
# リターンコード
#    0: 正常終了
#    6: エラー発生時
#
#--------------------------------------------------------------------------------------------------
function git.archive() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR TARGET OUTPUT_DIR"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # archive対象
  local _target="$2"

  #出力先ディレクトリ
  local _output_dir="$3"
  if [ ! -d ${_output_dir} ]; then
    log.debug_console "mkdir -p ${_output_dir}"
    mkdir -p ${_output_dir}
  fi

  # 出力先パス
  local _path_output=${_output_dir}/`echo ${_target} | sed "s|/|-|g"`.tar.gz


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # アーカイブ作成
  local _archive_prefix="$(echo ${_target} | sed 's|/|-|g')/"
  log.debug_console "git archive --worktree-attributes --format=tar --prefix=${_archive_prefix} ${_target} | gzip > ${_path_output}"
  git archive --worktree-attributes --format=tar --prefix=${_archive_prefix} ${_target} | gzip > ${_path_output}
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "アーカイブの作成に失敗しました。Git作業ディレクトリ：${_work_dir}、タグ：${_target}、出力先ディレクトリ：${_output_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   From・To間の差分ファイルのアーカイブを作成します。
#   ※ tar.gz 形式で出力します。
#
#   ・tar.gz解凍後のディレクトリ構成
#      - Fromタグ_Toタグ/ ※ From・Toタグに含まれる "/" は "-" に変換されます
#        - 差分ファイル群
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: Fromタグ ※ タグ / ブランチ / コミットハッシュ
#   ・3: Toタグ   ※ タグ / ブランチ / コミットハッシュ
#   ・4: 出力先ディレクトリ
#
# 出力
#   出力先ディレクトリ/${Fromタグ}_${Toタグ}.tar.gz
#
# リターンコード
#    0: 正常終了
#    6: エラー発生時
#
#--------------------------------------------------------------------------------------------------
function git.archive_diff() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR FROM_TAG TO_TAG OUTPUT_DIR"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 4 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Fromタグ
  local _from_tag="$2"

  # Toタグ
  local _to_tag="$3"

  # 出力先ディレクトリ
  local _output_dir="$4"
  if [ ! -d ${_output_dir} ]; then
    log.debug_console "mkdir -p ${_output_dir}"
    mkdir -p ${_output_dir}
  fi

  # 出力先パス
  local _path_output=${_output_dir}/`echo ${_from_tag} | sed "s|/|-|g"`_`echo ${_to_tag} | sed "s|/|-|g"`.tar.gz


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # 差分存在チェック
  log.debug_console "git diff --diff-filter=ACMR --name-only ${_from_tag} ${_to_tag}"
  local _diff_list=( `git diff --diff-filter=ACMR --name-only ${_from_tag} ${_to_tag}` )
  if [ ${#_diff_list[@]} -eq 0 ]; then
    # 差分なしの場合
    log.remove_indent
    return ${EXITCODE_WARN}
  fi

  # アーカイブ作成
  local _archive_prefix="$(echo ${_from_tag} | sed 's|/|-|g')_$(echo ${_to_tag} | sed 's|/|-|g')/"
  local _diff_path_list="$(git diff --diff-filter=ACMR --name-only ${_from_tag} ${_to_tag})"
  log.debug_console "git archive --worktree-attributes --format=tar --prefix=${_archive_prefix} ${_to_tag} ${_diff_path_list} | gzip > ${_path_output}"
  git archive --worktree-attributes --format=tar --prefix=${_archive_prefix} ${_to_tag} ${_diff_path_list} | gzip > ${_path_output}
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "アーカイブの作成に失敗しました。Git作業ディレクトリ：${_work_dir}、Fromタグ：${_from_tag}、Toタグ：${_to_tag}、出力先ディレクトリ：${_output_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   ブランチを追加します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: 作成元（ブランチ or タグ or コミットハッシュ）
#   ・3: 対象ブランチ名
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.branch_add() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR FROM TO_BRANCH"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 作成元
  local _from="$2"

  # 対象ブランチ名
  local _to="$3"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # 作業ディレクトリのブランチ切替え
  git.switch ${_work_dir} ${_from}
  _ret_code=$?
  if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
    log.error_console "作業ディレクトリのブランチ切替えに失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リセット
  git.reset "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # ブランチ作成
  log.debug_console "git checkout -b ${_to} ${_from}"
  log.add_indent
  git checkout -b "${_to}" "${_from}"                                                         2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ブランチの作成に失敗しました。Git作業ディレクトリ：${_work_dir}、作成元：${_from}、ブランチ名：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # プッシュ
  log.debug_console "git push origin ${_to}"
  log.add_indent
  git push origin "${_to}"                                                                    2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ブランチのpushに失敗しました。Git作業ディレクトリ：${_work_dir}、作成元：${_from}、ブランチ名：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   ブランチをリネームします。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: リネーム元ブランチ名
#   ・3: リネーム先ブランチ名
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.branch_rename() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR FROM_BRANCH TO_BRANCH"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リネーム元
  local _from="$2"

  # リネーム先
  local _to="$3"

  # リネーム元ブランチのprotect確認
  local _repo=$(basename ${_work_dir})
  local _group=$(basename $(dirname ${_work_dir}))
#  gitlab.is_protected_branch ${_group} ${_repo} ${_from}
#  if [ $? -eq ${EXITCODE_SUCCESS} ]; then
#    log.error_console "指定のブランチはprotectされています。Git作業ディレクトリ：${_work_dir}、対象ブランチ：${_from}"
#    log.remove_indent
#    return ${EXITCODE_ERROR}
#  fi


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # リセット
  git.reset "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # ローカルでのブランチリネーム
  log.debug_console "git branch -m ${_from} ${_to}"
  log.add_indent
  git branch -m "${_from}" "${_to}"                                                           2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ローカルブランチのリネームに失敗しました。Git作業ディレクトリ：${_work_dir}、リネーム元：${_from}、リネーム先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リネーム先ブランチ（追加）をプッシュ
  log.debug_console "git push origin ${_to}"
  log.add_indent
  git push origin "${_to}"                                                                    2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "リネーム先ブランチのpushに失敗しました。Git作業ディレクトリ：${_work_dir}、リネーム元：${_from}、リネーム先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リネーム元ブランチ（削除）をプッシュ
  log.debug_console "git push --delete origin ${_from}"
  log.add_indent
  git push --delete origin "${_from}"                                                         2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "リネーム元ブランチのpushに失敗しました。Git作業ディレクトリ：${_work_dir}、リネーム元：${_from}、リネーム先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   ブランチを削除します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: 削除対象
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.branch_remove() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR BRANCH"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 削除対象
  local _to="$2"

  # リネーム元ブランチのprotect確認
  local _repo=$(basename ${_work_dir})
  local _group=$(basename $(dirname ${_work_dir}))
#  gitlab.is_protected_branch ${_group} ${_repo} ${_to}
#  if [ $? -eq ${EXITCODE_SUCCESS} ]; then
#    log.error_console "指定のブランチはprotectされています。Git作業ディレクトリ：${_work_dir}、対象ブランチ：${_to}"
#    log.remove_indent
#    return ${EXITCODE_ERROR}
#  fi


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # リセット
  git.reset "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # Git作業ディレクトリの現在ブランチを確認
  local _cur_branch=`git branch | grep "\*" | sed -e 's|^\* ||'`
  if [ "${_cur_branch}" = "${_to}" ]; then
    # 削除対象ブランチをcheckoutしている場合、masterブランチに切替え
    log.debug_console "git checkout master"
    log.add_indent
    git checkout master                                                                       2>&1 |
    log.debug_console
    log.remove_indent
  fi

  # ローカルでのブランチ削除
  log.debug_console "git branch -D ${_to}"
  log.add_indent
  git branch -D "${_to}"                                                                      2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ローカルブランチの削除に失敗しました。Git作業ディレクトリ：${_work_dir}、対象ブランチ：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リネーム元ブランチ（削除）をプッシュ
  log.debug_console "git push --delete origin ${_to}"
  log.add_indent
  git push --delete origin "${_to}"                                                           2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ブランチ削除のpushに失敗しました。Git作業ディレクトリ：${_work_dir}、対象タグ：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   2つのブランチをマージします。
#   git mergeコマンドを利用して、明示的にコミット履歴を作成します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: マージ元ブランチ
#   ・3: マージ先ブランチ
#   ・4: コミットメッセージ ※任意
#
# オプション
#   --no-push：
#     マージコミットのリモートリポジトリへのプッシュをスキップします。
#
# 出力
#   なし
#
# 想定する利用パターン
#   featureでの開発完了時に feature/1 → develop へのマージ
#   releaseでの並走作業中に release/1 → release/2, release/2 → master へのマージ
#   releaseでの作業完了時に release/1 → develop, master へのマージ
#   hotfix での開発完了時に hotfix/1  → master  へのマージ
#   など
#
#--------------------------------------------------------------------------------------------------
function git.branch_merge() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR FROM_BRANCH TO_BRANCH [MESSAGE]"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  local _is_push=true

  # オプション解析
  while :; do
    case $1 in
      --no-push)
        _is_push=false
        shift
        ;;
      --)
        shift
        break
        ;;
      -*)
        log.error_console "${_USAGE}"
        log.remove_indent
        return ${EXITCODE_ERROR}
        ;;
      *)
        break
        ;;
    esac
  done

  # 引数の数
  if [ $# -lt 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # マージ元
  local _from="$2"

  # マージ先
  local _to="$3"

  # コミットコメント
  local _message="$4"
  if [ "${_message}" = "" ]; then
    _message="Merge branch '"${_from}"' into '"${_to}"'"
  fi


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # リセット
  git.reset "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # マージ元ブランチに切替え
  log.debug_console "git checkout ${_from}"
  log.add_indent
  git checkout "${_from}"                                                                     2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "マージ元ブランチへの作業ディレクトリの切替えに失敗しました。Git作業ディレクトリ：${_work_dir}、マージ元：${_from}、マージ先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # マージ元ブランチのpull
  git.pull ${_work_dir}
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "マージ元ブランチのプルに失敗しました。Git作業ディレクトリ：${_work_dir}、マージ元：${_from}、マージ先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # マージ先ブランチに切替え
  log.debug_console "git checkout ${_to}"
  log.add_indent
  git checkout "${_to}"                                                                       2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "マージ先ブランチへの作業ディレクトリの切替えに失敗しました。Git作業ディレクトリ：${_work_dir}、マージ元：${_from}、マージ先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # マージ先ブランチのpull
  git.pull ${_work_dir}
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "マージ先ブランチのプルに失敗しました。Git作業ディレクトリ：${_work_dir}、マージ元：${_from}、マージ先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # マージ
  log.debug_console "git merge --no-ff -m ${_message} ${_from}"
  log.add_indent
  git merge --no-ff -m "${_message}" "${_from}"                                               2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "マージに失敗しました。Git作業ディレクトリ：${_work_dir}、マージ元：${_from}、マージ先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  if [ "${_is_push}" = "true" ]; then
    # プッシュする場合 ※ --no-push オプションを指定していない場合

    # マージ結果をプッシュ
    log.debug_console "git push origin ${_to}"
    log.add_indent
    git push origin "${_to}"                                                                  2>&1 |
    log.debug_console
    _ret_code=${PIPESTATUS[0]}
    log.remove_indent

    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.error_console "マージ先ブランチのpushに失敗しました。Git作業ディレクトリ：${_work_dir}、マージ元：${_from}、マージ先：${_to}、リターンコード：${_ret_code}"
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   ローカルリポジトリにタグを追加します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: 作成元（ブランチ or コミットハッシュ）
#   ・3: 対象タグ名
#   ・4: タグに付与するメッセージ
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.tag_add_local() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR FROM TAG [MESSAGE]"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -lt 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 作成元
  local _from="$2"

  # 対象タグ名
  local _to="$3"

  # メッセージ
  local _message="$4"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # checkout時点のコミットにリセット
  git.local.reset_only ${_work_dir}
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # タグ追加処理前のカレントブランチを取得
  local _before_branch=`git rev-parse --abbrev-ref HEAD`

  # 作成元がブランチかチェック
  local _is_branch="false"
  if [ "$(git branch -a | grep ${_from})" != "" ]; then
    _is_branch="true"
  fi

  # 作成元をチェックアウト
  log.debug_console "git checkout ${_from}"
  log.add_indent
  git checkout "${_from}"                                                                     2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "作業ディレクトリの切替えに失敗しました。Git作業ディレクトリ：${_work_dir}、作成元：${_from}、タグ名：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # タグ作成
  log.debug_console "git tag -a ${_to} -m ${_message}"
  log.add_indent
  git tag -a "${_to}" -m "${_message}"                                                        2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "タグの作成に失敗しました。Git作業ディレクトリ：${_work_dir}、作成元：${_from}、タグ名：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  if [ "${_is_branch}" != "true" ]; then
    # 作成元がブランチ以外の場合 ※ checkout時に detached HEAD (HEAD がブランチから切り離されている) 状態になっている

    # タグ追加処理前のカレントブランチをチェックアウト
    log.debug_console "git checkout ${_before_branch}"
    log.add_indent
    git checkout "${_before_branch}"                                                          2>&1 |
    log.debug_console
    _ret_code=${PIPESTATUS[0]}
    log.remove_indent

    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.error_console "detached HEAD状態の解消でエラーが発生しました。リターンコード：${_ret_code}"
      log.remove_indent
      return ${EXITCODE_ERROR}
    fi

  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   タグを追加します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: 作成元（ブランチ or コミットハッシュ）
#   ・3: 対象タグ名
#   ・4: タグに付与するメッセージ
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.tag_add() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR FROM TAG [MESSAGE]"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -lt 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 作成元
  local _from="$2"

  # 対象タグ名
  local _to="$3"

  # メッセージ
  local _message="$4"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # 作業ディレクトリのブランチ切替え
  git.switch ${_work_dir} ${_from}
  _ret_code=$?
  if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
    log.error_console "作業ディレクトリのブランチ切替えに失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリの最新化
  git.reset ${_work_dir}
  _ret_code=$?
  if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
    log.error_console "Git作業ディレクトリの最新化に失敗しました。Git作業ディレクトリ：${_work_dir}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ローカルにタグ作成
  ${FUNCNAME[0]}_local $@
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # プッシュ
  log.debug_console "git push origin ${_to}"
  log.add_indent
  git push origin "${_to}"                                                                    2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "タグのpushに失敗しました。Git作業ディレクトリ：${_work_dir}、作成元：${_from}、タグ名：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   タグをローカルのみリネームします。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: リネーム元タグ名
#   ・3: リネーム先タグ名
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.tag_rename_local() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR FROM_TAG TO_TAG"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リネーム元
  local _from="$2"

  # リネーム先
  local _to="$3"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # checkout時点のコミットにリセット
  git.local.reset_only ${_work_dir}
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ローカルでTOタグ作成
  log.debug_console "git tag ${_to} ${_from}"
  log.add_indent
  git tag "${_to}" "${_from}"                                                                 2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ローカルでのリネーム先タグの作成に失敗しました。Git作業ディレクトリ：${_work_dir}、リネーム元：${_from}、リネーム先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ローカルでFROMタグ削除
  log.debug_console "git tag -d ${_from}"
  log.add_indent
  git tag -d "${_from}"                                                                       2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ローカルでのリネーム元タグの削除に失敗しました。Git作業ディレクトリ：${_work_dir}、リネーム元：${_from}、リネーム先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   タグをリネームします。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: リネーム元タグ名
#   ・3: リネーム先タグ名
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.tag_rename() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR FROM_TAG TO_TAG"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 3 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リネーム元
  local _from="$2"

  # リネーム先
  local _to="$3"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

 # ローカルのタグリネーム
  ${FUNCNAME[0]}_local $@
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # リネーム先タグ（追加）をプッシュ
  log.debug_console "git push origin ${_to}"
  log.add_indent
  git push origin "${_to}"                                                                    2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "リネーム先タグのpushに失敗しました。Git作業ディレクトリ：${_work_dir}、リネーム元：${_from}、リネーム先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # リネーム元タグ（削除）をプッシュ
  log.debug_console "git push --delete origin ${_from}"
  log.add_indent
  git push --delete origin "${_from}"                                                         2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "リネーム元タグのpushに失敗しました。Git作業ディレクトリ：${_work_dir}、リネーム元：${_from}、リネーム先：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   ローカルリポジトリのタグを削除します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: 削除対象
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.tag_remove_local() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR TAG"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 削除対象
  local _to="$2"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # checkout時点のコミットにリセット
  git.local.reset_only ${_work_dir}
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # ローカルでのタグ削除
  log.debug_console "git tag -d ${_to}"
  log.add_indent
  git tag -d "${_to}"                                                                         2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "ローカルタグの削除に失敗しました。Git作業ディレクトリ：${_work_dir}、対象タグ：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}

}



#--------------------------------------------------------------------------------------------------
# 概要
#   タグを削除します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: 削除対象
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.tag_remove() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR TAG"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 削除対象
  local _to="$2"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # ローカルのタグ削除
  ${FUNCNAME[0]}_local $@
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # リネーム元タグ（削除）をプッシュ
  log.debug_console "git push --delete origin ${_to}"
  log.add_indent
  git push --delete origin "${_to}"                                                           2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent

  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "タグ削除のpushに失敗しました。Git作業ディレクトリ：${_work_dir}、対象タグ：${_to}、リターンコード：${_ret_code}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${EXITCODE_SUCCESS}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   存在するタグか否かを判定します。
#
# 引数
#   ・1: Git作業ディレクトリ
#   ・2: 確認対象
#
# 出力
#   なし
#
# リターンコード
#   ・${EXITCODE_SUCCESS}: 存在する場合
#   ・${EXITCODE_WARN}   : 存在しない場合
#   ・${EXITCODE_ERROR}  : エラーが発生した場合
#
#--------------------------------------------------------------------------------------------------
function git.is_exist_tag() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} GIT_WORK_DIR TAG"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 確認対象
  local _to="$2"


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # Git作業ディレクトリに移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # タグ一覧から絞り込み
  log.debug_console "git tag | grep \"${_to}\""
  git tag                                                                                          |
  grep "${_to}" > /dev/null
  _ret_code=$?


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    # grepのリターンコードが0以外の場合、WARN
    return ${EXITCODE_WARN}
  fi
  return ${EXITCODE_SUCCESS}
}



#--------------------------------------------------------------------------------------------------
# 概要
#   新規にリポジトリを作成して、指定のGit作業ディレクトリから保持タグ数分のタグ情報を取得し
#   新規リポジトリに取得した情報でコミット・タグ付与を行います。
#   処理終了時に既存リポジトリを削除して、新規リポジトリを既存リポジトリの名前にリネームします。
#
# 引数
#   ・1: ハウスキープ対象のGit作業ディレクトリ
#   ・2: 最大保持タグ数
#
# 出力
#   なし
#
#--------------------------------------------------------------------------------------------------
function git.housekeep_local_repository() {
  #------------------------------------------------------------------------------------------------
  # 事前処理
  #------------------------------------------------------------------------------------------------
  local _USAGE="Usage: ${FUNCNAME[0]} HOUSEKEEP_GIT_WORK_DIR MAX_TAG_COUNT"
  log.debug_console "${FUNCNAME[0]} $@"
  log.add_indent

  # 引数の数
  if [ $# -ne 2 ]; then
    log.error_console "${_USAGE}"
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # Git作業ディレクトリ
  local _work_dir="$1"
  git.local.check_work_dir "${_work_dir}"
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 最大保持タグ数
  local _max_tag_count="$2"

  # 新規リポジトリパス
  local _new_repository_path="$1"_temp
  mkdir -p ${_new_repository_path}

  # 作業ディレクトリ
  local _dir_tmp=/tmp/${FUNCNAME[0]}_$$
  mkdir -p ${_dir_tmp}


  #------------------------------------------------------------------------------------------------
  # 本処理
  #------------------------------------------------------------------------------------------------
  local _ret_code=${EXITCODE_SUCCESS}

  # 新規リポジトリパスへ移動
  log.debug_console "cd ${_new_repository_path}"
  cd ${_new_repository_path}

  # Gitリポジトリとして初期化
  log.debug_console "git init"
  log.add_indent
  git init                                                                                    2>&1 |
  log.debug_console
  _ret_code=${PIPESTATUS[0]}
  log.remove_indent
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "Gitリポジトリの初期化に失敗しました。リポジトリパス：${_new_repository_path}、リターンコード：${_ret_code}"
    rm -rf ${_dir_tmp}
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  # ハウスキープ対象のリポジトリへ移動
  log.debug_console "cd ${_work_dir}"
  cd "${_work_dir}"

  # タグ取得 ※タグ付与日付の降順で取得
  log.debug_console "git for-each-ref --sort=-taggerdate --format='%(tag)' | grep -v ^\s*$"
  local _tags=( `git for-each-ref --sort=-taggerdate --format='%(tag)' | grep -v ^\s*$` )
  _ret_code=$?
  if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    log.error_console "タグの取得に失敗しました。Git作業ディレクトリ：${_work_dir}、、リターンコード：${_ret_code}"
    rm -rf ${_dir_tmp}
    log.remove_indent
    return ${EXITCODE_ERROR}
  fi

  # タグ数チェック
  if [ ${#_tags[@]} -le ${_max_tag_count} ]; then
    # 保持タグ数以下の場合
    log.debug_console "既存のタグ数が最大保持タグ数以下のためハウスキープ対象外です。Git作業ディレクトリ：${_work_dir}、最大保持タグ数：${_max_tag_count}、タグ数：${#_tags[@]}"
    rm -rf ${_new_repository_path}
    rm -rf ${_dir_tmp}
    cd - > /dev/null 2>&1
    log.remove_indent
    return ${EXITCODE_SUCCESS}
  fi

  # タグ絞り込み  ※タグ追加日時の新しい順にループ
  local _target_tags=()
  local _cur_index=0
  while [ ${_cur_index} -lt ${_max_tag_count} ]; do
    _target_tags+=( "${_tags[${_cur_index}]}" )
    _cur_index=`expr ${_cur_index} + 1`
  done

  # ハウスキープ ※タグ追加日時の古い順にループ
  log.add_indent
  _cur_index=`expr ${_max_tag_count} - 1`
  while [ ${_cur_index} -ge 0 ]; do
    # tar.gzファイル名
    local _cur_zip_filename=`echo ${_target_tags[${_cur_index}]} | sed "s|/|-|g"`.tar.gz
    # 解凍ディレクトリルート
    local _cur_unzip_root_dir=`echo ${_target_tags[${_cur_index}]} | sed "s|/|-|g"`

    log.debug_console "タグ：${_target_tags[${_cur_index}]}"
    log.add_indent

    # ハウスキープ対象Git作業ディレクトリからタグ情報抽出
    git.archive ${_work_dir} ${_target_tags[${_cur_index}]} ${_dir_tmp}/old
    _ret_code=$?
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      _ret_code=${EXITCODE_ERROR}
      log.remove_indent
      break
    fi

    # 解凍
    log.debug_console "tar -zxf ${_dir_tmp}/old/${_cur_zip_filename} -C ${_dir_tmp}/old"
    tar -zxf ${_dir_tmp}/old/${_cur_zip_filename} -C ${_dir_tmp}/old > /dev/null 2>&1
    _ret_code=$?
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      log.error_console "取得したタグ情報の展開に失敗しました。タグ：${_target_tags[${_cur_index}]}、リターンコード：${_ret_code}"
      _ret_code=${EXITCODE_ERROR}
      log.remove_indent
      break
    fi

    # 新規リポジトリ配下のファイルを洗い替え
    log.debug_console "rm -rf ${_new_repository_path}/*"
    rm -rf ${_new_repository_path}/*

    log.debug_console "cp -rf ${_dir_tmp}/old/${_cur_unzip_root_dir}/ ${_new_repository_path}/"
    cp -rf ${_dir_tmp}/old/${_cur_unzip_root_dir}/ ${_new_repository_path}/

    local _cur_commit_comment="housekeep commit."
    local _is_empty_commit=false

    # コミット用にステージング
    git.staging "${_new_repository_path}"
    _ret_code=$?
    if [ ${_ret_code} -eq ${EXITCODE_SUCCESS} ]; then
      # コミット実施
      git.commit ${_new_repository_path} "${_cur_commit_comment}"
      _ret_code=$?
      if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
        _ret_code=${EXITCODE_ERROR}
        log.remove_indent
        break
      fi

    elif [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
      # ステージング対象が存在しない場合、emptyコミット
      _is_empty_commit=true
      log.debug_console "cd ${_new_repository_path}"
      cd ${_new_repository_path}

      log.debug_console "git commit --allow-empty -m \"${_cur_commit_comment}\""
      log.add_indent
      git commit --allow-empty -m "${_cur_commit_comment}"                                    2>&1 |
      log.debug_console
      _ret_code=${PIPESTATUS[0]}
      log.remove_indent

      log.debug_console "cd -"
      cd - > /dev/null

      if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
        _ret_code=${EXITCODE_ERROR}
        log.remove_indent
        break
      fi

    elif [ ${_ret_code} -eq ${EXITCODE_ERROR} ]; then
      _ret_code=${EXITCODE_ERROR}
      log.remove_indent
      break
    fi

    # タグ付与
    git.tag_add_local ${_new_repository_path} master ${_target_tags[${_cur_index}]}
    _ret_code=$?
    if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
      _ret_code=${EXITCODE_ERROR}
      log.remove_indent
      break
    fi

    if [ "${_is_empty_commit}" = "true" ]; then
      # emtpyコミットの場合、内容比較をスキップ
      log.warn_console "タグ：${_target_tags[${_cur_index}]} で更新されたファイルは存在しないため、内容比較をスキップします。"

    else
      # emtpyコミット以外の場合、内容比較を実施
      # 新規リポジトリから付与したタグ情報を抽出
      git.archive ${_new_repository_path} ${_target_tags[${_cur_index}]} ${_dir_tmp}/new
      _ret_code=$?
      if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
        _ret_code=${EXITCODE_ERROR}
        log.remove_indent
        break
      fi

      # 解凍
      log.debug_console "tar -zxf ${_dir_tmp}/new/${_cur_zip_filename} -C ${_dir_tmp}/new"
      tar -zxf ${_dir_tmp}/new/${_cur_zip_filename} -C ${_dir_tmp}/new > /dev/null 2>&1
      _ret_code=$?
      if [ ${_ret_code} -ne ${EXITCODE_SUCCESS} ]; then
        log.error_console "取得したタグ情報の展開に失敗しました。タグ：${_target_tags[${_cur_index}]}、リターンコード：${_ret_code}"
        _ret_code=${EXITCODE_ERROR}
        log.remove_indent
        break
      fi

      # 比較
      log.debug_console "${DIR_BIN_LIB}/dir_diff.sh ${_dir_tmp}/old/${_cur_unzip_root_dir} ${_dir_tmp}/new/${_cur_unzip_root_dir}"
      log.add_indent
      ${DIR_BIN_LIB}/dir_diff.sh ${_dir_tmp}/old/${_cur_unzip_root_dir} ${_dir_tmp}/new/${_cur_unzip_root_dir} 2>&1 |
      log.debug_console
      _ret_code=${PIPESTATUS[0]}
      log.remove_indent
      if [ ${_ret_code} -eq ${EXITCODE_WARN} ]; then
        # 差異がある場合
        log.error_console "ハウスキープ対象リポジトリと新規リポジトリで差異が発生しました。ハウスキープ対象リポジトリ：${_work_dir}、タグ：${_target_tags[${_cur_index}]}"
        _ret_code=${EXITCODE_ERROR}
        log.remove_indent
        break

      elif [ ${_ret_code} -eq ${EXITCODE_ERROR} ]; then
        _ret_code=${EXITCODE_ERROR}
        log.remove_indent
        break
      fi
    fi


    log.remove_indent

    # 次のタグへ
    _cur_index=`expr ${_cur_index} - 1`

  done
  log.remove_indent

  # 結果チェック
  if [ ${_ret_code} -eq ${EXITCODE_SUCCESS} ]; then
    # 正常にハウスキープできた場合

    # ハウスキープ対象Git作業ディレクトリ削除
    log.debug_console "rm -rf ${_work_dir}"
    rm -rf ${_work_dir}

    # 新規リポジトリをリネーム
    log.debug_console "mv ${_new_repository_path} ${_work_dir}"
    mv ${_new_repository_path} ${_work_dir}

  else
    # エラーが発生した場合
    log.error_console "ハウスキープ中にエラーが発生しました。ハウスキープ対象リポジトリ：${_work_dir}"

    # 新規リポジトリ削除
    log.debug_console "rm -rf ${_new_repository_path}"
    rm -rf ${_new_repository_path}

  fi


  #------------------------------------------------------------------------------------------------
  # 事後処理
  #------------------------------------------------------------------------------------------------
  # 一時作業ディレクトリ削除
  log.debug_console "rm -rf ${_dir_tmp}"
  rm -rf ${_dir_tmp}

  # 元ディレクトリに移動
  log.debug_console "cd -"
  cd - > /dev/null 2>&1

  log.remove_indent
  return ${_ret_code}

}
