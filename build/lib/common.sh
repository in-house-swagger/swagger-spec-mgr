#!/bin/bash
#---------------------------------------------------------------------------------------------------
# git設定
#---------------------------------------------------------------------------------------------------
function add_git_config() {
  echo "  ${FUNCNAME[0]}"

  if [[ "$(git config --get user.name)x" = "x" ]]; then
    git config user.name "${COMMIT_USER}"
    git config user.email "${COMMIT_MAIL}"
  fi

  echo "https://${GITHUB_TOKEN}:@github.com" > "${PATH_CREDENTIALS}"
  git config credential.helper "store --file=${PATH_CREDENTIALS}"
  exit_on_fail "${FUNCNAME[0]}" $?

  return 0
}


#---------------------------------------------------------------------------------------------------
# 認証ファイル削除
#---------------------------------------------------------------------------------------------------
function remove_credential() {
  echo "  ${FUNCNAME[0]}"

  if [[ -f "${PATH_CREDENTIALS}" ]]; then rm -f "${PATH_CREDENTIALS}"; fi
  return 0
}


#---------------------------------------------------------------------------------------------------
# 終了処理
#---------------------------------------------------------------------------------------------------
function exit_on_fail() {
  local _proc_name="$1"
  local _retcode="$2"

  if [[ ${_retcode} -ne 0 ]]; then
    remove_credential
    echo "error occured in ${_proc_name}." >&2
    exit ${_retcode}
  fi
  return 0
}


#---------------------------------------------------------------------------------------------------
# バージョン取得
#---------------------------------------------------------------------------------------------------
function get_version() {
  local _version=$(                                                                                \
    cat pom.xml                                                                                    |
    grep "^    <version"                                                                           |
    sed -e "s|^  *||g"                                                                             |
    sed -e "s|<version>||"                                                                         |
    sed -e "s|</version>||"                                                                        \
  )
  echo "${_version}"
}

function update_version_file() {
  local _cur_version="$1"
  local _release_version="$2"

  echo "  update version file"
  echo "    ${_cur_version} -> ${_release_version}"
  cp pom.xml pom.xml.tmp
  cat pom.xml.tmp                                                                                  |
  sed -e "s|<version>${_cur_version}</version>|<version>${_release_version}</version>|" >pom.xml
  rm -f pom.xml.tmp
}
