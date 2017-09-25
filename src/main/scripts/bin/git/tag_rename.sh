#!/bin/bash
#set -eux
#==================================================================================================
#
# タグリネーム処理
#
# 引数
#   1: リネーム元タグ名
#   2: リネーム先タグ名
#   3: コミットユーザ ※任意
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
  echo "Usage: $(basename $0) FROM_TAG TO_TAG [USER]" >&2
  exit ${EXITCODE_ERROR}
}



#--------------------------------------------------------------------------------------------------
# 事前処理
#--------------------------------------------------------------------------------------------------
raw_args="$*"
#ret_code=${EXITCODE_SUCCESS}


#--------------------------------------------------------------------------------
# オプション解析
#--------------------------------------------------------------------------------
while :; do
  case $1 in
    -h|--help)
      usage
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
# 開始ログ
#--------------------------------------------------------------------------------
log.save_indent
log.info_teelog "START --- $(basename $0) ${raw_args}"
log.add_indent


#--------------------------------------------------------------------------------
# 引数取得
#--------------------------------------------------------------------------------
# 引数チェック
if [ $# -lt 2 ] || [ $# -gt 3 ]; then
  usage
fi

# リネーム元タグ名
from_tag="$1"
if [ "${from_tag}x" = "x" ]; then
  git.common.exit_script ${EXITCODE_ERROR} "リネーム元タグ名 が指定されていません。"
fi

# リネーム先タグ名
to_tag="$2"
if [ "${to_tag}x" = "x" ]; then
  git.common.exit_script ${EXITCODE_ERROR} "リネーム先タグ名 が指定されていません。"
fi

# コミットユーザ
user="$3"



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


#--------------------------------------------------------------------------------
# tag_rename
#--------------------------------------------------------------------------------
has_origin=$(git.has_origin "${dir_repo}")
if [ $? -ne ${EXITCODE_SUCCESS} ]; then
  git.common.exit_script ${EXITCODE_ERROR} "リモートリポジトリの存在確認 でエラーが発生しました。"
fi

if [ "${has_origin}" != "true" ]; then
  # 存在しない場合、localのみ
  git.tag_rename_local "${dir_repo}" "${from_tag}" "${to_tag}"                                2>&1 | tee -a "${PATH_LOG}"
else
  # 存在する場合、pushあり
  git.tag_rename "${dir_repo}" "${from_tag}" "${to_tag}"                                      2>&1 | tee -a "${PATH_LOG}"
fi
ret_code=${PIPESTATUS[0]}

if [ ${ret_code} -ne ${EXITCODE_SUCCESS} ]; then
  git.common.exit_script ${EXITCODE_ERROR} "タグのリネーム でエラーが発生しました。"
fi



#--------------------------------------------------------------------------------------------------
# 事後処理
#--------------------------------------------------------------------------------------------------
git.common.exit_script ${EXITCODE_SUCCESS} "${EXITMSG_SUCCESS}"
