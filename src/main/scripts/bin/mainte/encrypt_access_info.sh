#!/bin/bash
#set -eux
#==================================================================================================
#
# 接続情報の暗号化
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

# ログ出力ユーティリティ
. "${DIR_BIN_LIB}/logging_utils.sh"



#--------------------------------------------------------------------------------------------------
# 関数定義
#--------------------------------------------------------------------------------------------------
#--------------------------------------------------------------------------------
# Usage
#--------------------------------------------------------------------------------
function usage() {
  echo "Usage: $(basename $0)" >&2
  exit ${EXITCODE_ERROR}
}

#--------------------------------------------------------------------------------
# exit script
#
# 引数
#  ・1: EXIT CODE
#  ・2: メッセージ
#--------------------------------------------------------------------------------
function exit_script() {
  local _exit_code=$1
  local _exit_msg=$2

  # 終了ログ
  log.restore_indent
  if [ ${_exit_code} -eq ${EXITCODE_SUCCESS} ]; then
    log.info_teelog "${_exit_msg}"
    log.info_teelog "exit_code: ${_exit_code}"
    log.info_teelog "END   --- $(basename $0)"
  elif [ ${_exit_code} -eq ${EXITCODE_WARN} ]; then
    log.warn_teelog "${_exit_msg}"
    log.warn_teelog "exit_code: ${_exit_code}"
    log.warn_teelog "END   --- $(basename $0)"
  else
    log.error_teelog "${_exit_msg}"
    log.error_teelog "exit_code: ${_exit_code}"
    log.error_teelog "END   --- $(basename $0)"
  fi

  # ログローテーション（日次） ※先頭行判断
  log.rotatelog_by_day_first

  # 終了
  exit ${_exit_code}
}


#--------------------------------------------------------------------------------------------------
# 事前処理
#--------------------------------------------------------------------------------------------------
log.save_indent
log.info_teelog "START --- $(basename $0) $*"
log.add_indent
ret_code=${EXITCODE_SUCCESS}

#--------------------------------------------------------------------------------
# オプション解析
#--------------------------------------------------------------------------------
while :; do
  case $1 in
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
if [ $# -ne 0 ]; then
  usage
fi

#--------------------------------------------------------------------------------------------------
# 本処理
#--------------------------------------------------------------------------------------------------
is_encrypted=false

if [ ! -f ${PATH_ACCESS_INFO_RAW} ]; then
  log.info_teelog "接続情報ファイル：${PATH_ACCESS_INFO_RAW} が存在しません。暗号化をスキップします。"

else
  is_encrypted=true

  log.info_teelog "暗号化"
  log.add_indent
  log.debug_teelog "cat \"${PATH_ACCESS_INFO_RAW}\" | _encrypt > \"${PATH_ACCESS_INFO}\""
  cat "${PATH_ACCESS_INFO_RAW}"                                                                    |
  _encrypt                                                                                           > "${PATH_ACCESS_INFO}"
  ret_code=$?
  if [ ${ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    exit_script ${EXITCODE_ERROR} "暗号化に失敗しました。対象ファイル: ${PATH_ACCESS_INFO_RAW}"
  fi
  log.remove_indent

  log.info_teelog "暗号化前ファイルの削除"
  log.add_indent
  log.debug_teelog "rm -f \"${PATH_ACCESS_INFO_RAW}\""
  rm -f "${PATH_ACCESS_INFO_RAW}"
  ret_code=$?
  if [ ${ret_code} -ne ${EXITCODE_SUCCESS} ]; then
    exit_script ${EXITCODE_ERROR} "暗号化前ファイルの削除に失敗しました。対象ファイル: ${PATH_ACCESS_INFO_RAW}"
  fi
  log.remove_indent
fi

log.remove_indent


#--------------------------------------------------------------------------------------------------
# 事後処理
#--------------------------------------------------------------------------------------------------
# 暗号化実施判定
if [ "${is_encrypted}" != "true" ]; then
  exit_script ${EXITCODE_WARN} "暗号化対象が存在しませんでした。"
fi

exit_script ${EXITCODE_SUCCESS} "${EXITMSG_SUCCESS}"
