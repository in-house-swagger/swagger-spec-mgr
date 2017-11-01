#!/bin/bash
#set -x
#==================================================================================================
#
# リポジトリ初期化処理
#   GIT_REMOTE_REPOSITORY_URL が定義されている場合、git clone
#   定義されていない場合、git init を実行します。
#
# 引数
#   1: コミットユーザ      ※任意
#   2: コミットユーザemail ※任意
#
# オプション
#   -f | --force: 強制初期化モード
#     Git作業ディレクトリが既に存在する場合、削除してから初期化します。
#
#==================================================================================================
#--------------------------------------------------------------------------------------------------
# 環境設定
#--------------------------------------------------------------------------------------------------
# カレントディレクトリの移動
dir_script="$(dirname $0)"
cd "$(cd ${dir_script}; pwd)" || exit 1
# 共通設定
readonly DIR_BASE=$(cd ../..; pwd)
. ../setenv


#--------------------------------------------------------------------------------
# ユーティリティ読み込み
#--------------------------------------------------------------------------------
# ログ出力ユーティリティ
. "${DIR_BIN_LIB}/logging_utils.sh"
# gitユーティリティ
. "${DIR_BIN_LIB}/git_utils.sh"
# git操作バウンダリ共通処理
. "${DIR_BIN}/git/_common.sh"



#--------------------------------------------------------------------------------------------------
# 関数定義
#--------------------------------------------------------------------------------------------------
#--------------------------------------------------------------------------------
# Usage
#--------------------------------------------------------------------------------
function usage() {
  echo "Usage: $(basename $0) [USER] [EMAIL]" >&2
  exit ${EXITCODE_ERROR}
}


#--------------------------------------------------------------------------------
# ローカルリポジトリ作成
#
# 引数
#   1: Git作業ディレクトリ
#--------------------------------------------------------------------------------
function local.init() {
  local _dir_repo="$1"

  # リポジトリ作成
  git.init "${_dir_repo}"
  if [ $? -ne ${EXITCODE_SUCCESS} ]; then
    return ${EXITCODE_ERROR}
  fi

  # first commit (empty)
  git.commit "${_dir_repo}" "first commit" "--allow-empty"
  if [ $? -ne ${EXITCODE_SUCCESS} ]; then
    return ${EXITCODE_ERROR}
  fi

  return ${EXITCODE_SUCCESS}
}


#--------------------------------------------------------------------------------
# リモートリポジトリのclone
#
# 引数
#   1: Git作業ディレクトリ
#   2: コミットユーザ
#--------------------------------------------------------------------------------
function local.clone() {
  local _dir_repo="$1"

  # リポジトリ clone
  git.clone "${GIT_REMOTE_REPOSITORY_URL}" "${_dir_repo}"
  if [ $? -ne ${EXITCODE_SUCCESS} ]; then
    return ${EXITCODE_ERROR}
  fi

  # 認証情報ファイル追記
  git.add_credential "${GIT_REMOTE_REPOSITORY_URL}" "${GIT_ACCESS_USER}" "${GIT_ACCESS_PASSWORD}"
  if [ $? -ne ${EXITCODE_SUCCESS} ]; then
    return ${EXITCODE_ERROR}
  fi

  # 認証情報設定
  git.set_config "${_dir_repo}" "credential.helper" "store --file ${GIT__PATH_CRED}"
  if [ $? -ne ${EXITCODE_SUCCESS} ]; then
    return ${EXITCODE_ERROR}
  fi

  return ${EXITCODE_SUCCESS}
}


#--------------------------------------------------------------------------------
# ローカルリポジトリの設定追加（user.name, user.email）
#
# 引数
#  ・1: Git作業ディレクトリ
#  ・2: コミットユーザ
#  ・3: コミットユーザemail
#--------------------------------------------------------------------------------
function local.set_config() {
  local _dir_repo="$1"
  local _user="$2"
  if [ "${_user}x" = "x" ]; then
    _user="${SCM_DEFAULT_USER}"
  fi
  local _email="$3"
  if [ "${_email}x" = "x" ]; then
    _email="${SCM_DEFAULT_EMAIL}"
  fi

  git.set_config "${_dir_repo}" "user.name" "${_user}"
  if [ $? -ne ${EXITCODE_SUCCESS} ]; then
    return ${EXITCODE_ERROR}
  fi

  git.set_config "${_dir_repo}" "user.email" "${_email}"
  if [ $? -ne ${EXITCODE_SUCCESS} ]; then
    return ${EXITCODE_ERROR}
  fi

  return ${EXITCODE_SUCCESS}
}



#--------------------------------------------------------------------------------------------------
# 事前処理
#--------------------------------------------------------------------------------------------------
raw_args="$*"
ret_code=${EXITCODE_SUCCESS}

is_force="false"


#--------------------------------------------------------------------------------
# オプション解析
#--------------------------------------------------------------------------------
while :; do
  case $1 in
    -h|--help)
      usage
      ;;
    -f|--force)
      is_force="true"
      shift
      break
      ;;
    --)
      shift
      break
      ;;
    -*)
      usage
      ;;
    *)
      break
      ;;
  esac
done


#--------------------------------------------------------------------------------
# 引数取得
#--------------------------------------------------------------------------------
# 引数チェック
if [ $# -gt 2 ]; then
  usage
fi

# 開始ログ
log.start_script "$0" "${raw_args}"

# コミットユーザ
user="$1"

# コミットユーザemail
email="$2"



#--------------------------------------------------------------------------------------------------
# 本処理
#--------------------------------------------------------------------------------------------------
#--------------------------------------------------------------------------------
# Git作業ディレクトリ取得
#--------------------------------------------------------------------------------
dir_repo="$(git.common.get_repo_dir ${user})"
if [ $? -ne ${EXITCODE_SUCCESS} ]; then
  return ${EXITCODE_ERROR}
fi

# 強制初期化モードの場合、Git作業ディレクトリを削除
if [ "${is_force}" = "true" ] && [ -d "${dir_repo}" ]; then
  rm -fr "${dir_repo}"
fi


#--------------------------------------------------------------------------------
# init | clone
#--------------------------------------------------------------------------------
# GIT_REMOTE_REPOSITORY_URL の定義チェック
if [ "${GIT_REMOTE_REPOSITORY_URL}x" = "x" ]; then
  # 定義されていない場合、init
  local.init "${dir_repo}"                                                                    2>&1 | log.tee
else
  # 定義されている場合、clone
  local.clone "${dir_repo}" "${user}"                                                         2>&1 | log.tee
fi
ret_code=${PIPESTATUS[0]}

if [ ${ret_code} -ne ${EXITCODE_SUCCESS} ]; then
  git.common.exit_script ${EXITCODE_ERROR} "リポジトリの初期化でエラーが発生しました。"
fi


#--------------------------------------------------------------------------------
# リポジトリ設定の追加
#--------------------------------------------------------------------------------
local.set_config "${dir_repo}" "${user}" "${email}"                                           2>&1 | log.tee
ret_code=${PIPESTATUS[0]}

if [ ${ret_code} -ne ${EXITCODE_SUCCESS} ]; then
  git.common.exit_script ${EXITCODE_ERROR} "リポジトリ設定の追加でエラーが発生しました。"
fi



#--------------------------------------------------------------------------------------------------
# 事後処理
#--------------------------------------------------------------------------------------------------
git.common.exit_script ${EXITCODE_SUCCESS} "${EXITMSG_SUCCESS}"
