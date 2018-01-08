#!/bin/bash
#set -eux
#===================================================================================================
#
# Publish Documents
#
# env
#   GITHUB_TOKEN
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

version=$(get_version)


#---------------------------------------------------------------------------------------------------
# check
#---------------------------------------------------------------------------------------------------
if [[ "${GITHUB_TOKEN}x" = "x" ]]; then
  echo "GITHUB_TOKEN is not defined." >&2
  exit 1
fi


#---------------------------------------------------------------------------------------------------
# main
#---------------------------------------------------------------------------------------------------
echo "$(basename $0)"
retcode=0

echo "  clone"
rm -rf gh_pages
git clone -b "${BRANCH_GHPAGES}" "${GIT_URL}" gh_pages
exit_on_fail "clone" $?

echo "  update files"
before_dir="$(pwd)"
rm -rf gh_pages/*
cp -a "${DIR_DOCS}"/* gh_pages/
cd gh_pages

add_git_config

echo "  staging"
git add --all .
exit_on_fail "staging" $?

echo "  commit"
git commit -m "${MSG_PREFIX_RELEASE}v${version}"
exit_on_fail "commit" $?

echo "  push"
git push origin "${BRANCH_GHPAGES}"
exit_on_fail "push" $?

echo "  clear clone dir"
cd "${before_dir}"
rm -rf gh_pages/


#---------------------------------------------------------------------------------------------------
# teardown
#---------------------------------------------------------------------------------------------------
if [[ ${retcode} -eq 0 ]]; then
  echo "$(basename $0) success."
  exitcode=0
else
  echo "$(basename $0) failed." >&2
  exitcode=1
fi

remove_credential
exit ${exitcode}
