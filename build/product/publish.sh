#!/bin/bash
#set -eux
#===================================================================================================
#
# Publish Distribution Package
#
# env
#   BINTRAY_USER
#   BINTRAY_TOKEN
#
#===================================================================================================
#---------------------------------------------------------------------------------------------------
# env
#---------------------------------------------------------------------------------------------------
dir_script="$(dirname $0)"
cd "$(cd ${dir_script}; cd ../..; pwd)" || exit 1

readonly DIR_BASE="$(pwd)"
. "${DIR_BASE}/build/env.properties"
. "${DIR_BUILD_LIB}/common.sh"

readonly BINTRAY_URL="https://api.bintray.com"
readonly BINTRAY_ORG="in-house-swagger"
readonly BINTRAY_REPO="swagger-spec-mgr"

readonly BINTRAY_PKG_RELEASE="release"
readonly BINTRAY_PKG_SNAPSHOT="snapshot"

version=$(get_version)
_path_tmp_dist_list="/tmp/$(basename $0)_dist_list_$$"
_path_tmp_retcode_list="/tmp/$(basename $0)_retcode_list_$$"



#---------------------------------------------------------------------------------------------------
# functions
#---------------------------------------------------------------------------------------------------
#-------------------------------------------------------------------------------
# upload bintray
#-------------------------------------------------------------------------------
function upload_bintray() {
  echo "  ${FUNCNAME[0]} $*"

  local _package="$1"
  local _version="$2"
  local _filepath="$3"

  local _dir=$(dirname "${_filepath}")
  local _filename=$(basename "${_filepath}")

  echo "    upload start         : ${_filename}"
  local _url="${BINTRAY_URL}/content/${BINTRAY_ORG}/${BINTRAY_REPO}/${_package}/${_version}/${_filename}?publish=1&override=1"
  local _output_path="/tmp/${FUNCNAME[0]}_upload_${_filename}_$$"

  cd "${_dir}"
  local _cur_status=$(                                                                             \
    curl                                                                                           \
      --silent                                                                                     \
      --request PUT                                                                                \
      --write-out '%{http_code}'                                                                   \
      --output "${_output_path}"                                                                   \
      --user "${BINTRAY_USER}:${BINTRAY_TOKEN}"                                                    \
      --upload-file "${_filename}"                                                                 \
      "${_url}"                                                                                    \
    2>/dev/null                                                                                    \
  )
  echo "    upload end           : ${_filename} response=${_cur_status}"
  cd - >/dev/null 2>&1

  if [[ "$(echo ${_cur_status} | cut -c 1)" != "2" ]]; then
    cat "${_output_path}"
    echo ""
    rm -f "${_output_path}"
    return 1
  fi
# TODO list_in_downloads: true でリクエストしなくても、DLできる様子。
#  # snapshot版の場合、downloads表示なし
#  if [[ "${_package}" = "${BINTRAY_PKG_SNAPSHOT}" ]]; then return 0; fi
#
#  echo "    wait for bintray     : ${_filename}"
#  sleep 10
#
#  echo "    publish dl-list start: ${_filename}"
#  _url="${BINTRAY_URL}/file_metadata/${BINTRAY_ORG}/${BINTRAY_REPO}/${_filename}"
#  _output_path="/tmp/${FUNCNAME[0]}_publish-dl-list_${_filename}_$$"
#
#  _cur_status=$(                                                                                   \
#    curl                                                                                           \
#      --silent                                                                                     \
#      --request PUT                                                                                \
#      --write-out '%{http_code}'                                                                   \
#      --output "${_output_path}"                                                                   \
#      --user "${BINTRAY_USER}:${BINTRAY_TOKEN}"                                                    \
#      --header "Content-Type: application/json"                                                    \
#      --data-binary '{ "list_in_downloads" : true }'                                               \
#      "${_url}"                                                                                    \
#    2>/dev/null                                                                                    \
#  )
#  echo "    publish dl-list end  : ${_filename} responnse=${_cur_status}"
#
#  if [[ "$(echo ${_cur_status} | cut -c 1)" != "2" ]]; then
#    cat "${_output_path}"
#    echo ""
#    rm -f "${_output_path}"
#    return 1
#  fi

  return 0
}


#---------------------------------------------------------------------------------------------------
# check
#---------------------------------------------------------------------------------------------------
if [[ "${BINTRAY_USER}x" = "x" ]]; then
  echo "BINTRAY_USER is not defined." >&2
  exit 1
fi

if [[ "${BINTRAY_TOKEN}x" = "x" ]]; then
  echo "BINTRAY_TOKEN is not defined." >&2
  exit 1
fi


#-------------------------------------------------------------------------------
# オプション解析
#-------------------------------------------------------------------------------
package="${BINTRAY_PKG_RELEASE}"

while :; do
  case $1 in
    -s|--snapshot)
      package="${BINTRAY_PKG_SNAPSHOT}"
      # bintrayが"-SNAPSHOT"バージョンを弾くので".SNAPSHOT"にリネーム
      version="${version//-/.}"
      shift
      ;;

    --)
      shift
      break
      ;;

    *)
      break
      ;;
  esac
done

# releaseパッケージへの公開の場合
if [[ "${package}" = "${BINTRAY_PKG_RELEASE}" ]] &&
   [[ "${version}" != "${version//-SNAPSHOT/}" ]]; then
  echo "SNAPSHOT version can not publish to the release package. version=${version}" >&2
  exit 1
fi


#---------------------------------------------------------------------------------------------------
# main
#---------------------------------------------------------------------------------------------------
echo "$(basename $0)"
retcode=0

find "${DIR_DIST}" -mindepth 1 -maxdepth 1 -type f | sort >"${_path_tmp_dist_list}"
for _cur_path in $(cat "${_path_tmp_dist_list}"); do
  (
    upload_bintray "${package}" "${version}" "${_cur_path}"
    retcode=$?
    echo -n "${retcode}" >>"${_path_tmp_retcode_list}"
  ) &
done
wait


#---------------------------------------------------------------------------------------------------
# teardown
#---------------------------------------------------------------------------------------------------
retcodes="$(cat ${_path_tmp_retcode_list})"
if [[ "${retcodes//0/}x" = "x" ]]; then
  echo "$(basename $0) success."
  exitcode=0
else
  echo "$(basename $0) failed." >&2
  exitcode=1
fi

rm -f "${_path_tmp_dist_list}"
rm -f "${_path_tmp_retcode_list}"
exit ${exitcode}
