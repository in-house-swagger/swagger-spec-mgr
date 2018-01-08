#!/bin/bash
#set -eux
#===================================================================================================
#
# Pre Release
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


#---------------------------------------------------------------------------------------------------
# check
#---------------------------------------------------------------------------------------------------
if [[ "${GITHUB_TOKEN}x" = "x" ]]; then
  echo "GITHUB_TOKEN is not defined." >&2
  exit 1
fi

if [[ "$(which node)x" = "x" ]]; then
  echo "nodejs is not installed." >&2
  exit 1
fi

if [[ "$(which npm)x" = "x" ]]; then
  echo "npm is not installed." >&2
  exit 1
fi

if [[ "$(which conventional-changelog)x" = "x" ]]; then
  echo "conventional-changelog-cli is not installed." >&2
  exit 1
fi


#-------------------------------------------------------------------------------
# オプション解析
#-------------------------------------------------------------------------------
is_dry_run="false"

while :; do
  case $1 in
    -d|--dry-run)
      is_dry_run="true"
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


#---------------------------------------------------------------------------------------------------
# main
#---------------------------------------------------------------------------------------------------
echo "$(basename $0)"

echo ""
${DIR_BUILD}/product/build.sh
exit_on_fail "product/build" $?

_cur_version=$(get_version)
_release_version="${_cur_version//-SNAPSHOT/}"
_release_tag="v${_release_version}"
_commit_message="${MSG_PREFIX_RELEASE}${_release_tag}"

echo ""
update_version_file "${_cur_version}" "${_release_version}"

echo ""
add_git_config

echo "  git commit (before changelog)"
git add --all .
git commit -m "${_commit_message} (before changelog)"
exit_on_fail "git commit" $?

echo "  git tag (before changelog)"
git tag -a "${_release_tag}" -m "${_commit_message} (before changelog)"
exit_on_fail "git tag" $?


echo ""
echo "  generate changelog"
conventional-changelog -p angular -i CHANGELOG.md -s -r 0
exit_on_fail "generate changelog" $?

echo "  git commit"
git add --all .
git commit -m "${_commit_message}"
exit_on_fail "git commit" $?

echo "  git tag"
git tag -d "${_release_tag}"
git tag -a "${_release_tag}" -m "${_commit_message}"
exit_on_fail "git tag" $?


if [[ "${is_dry_run}" != "true" ]]; then
  echo ""
  echo "  git push branch ${BRANCH_MASTER}"
  git push origin "${BRANCH_MASTER}"
  exit_on_fail "git push branch ${BRANCH_MASTER}" $?

  echo "  git push tag ${_release_tag}"
  git push origin "${_release_tag}"
  exit_on_fail "git push tag ${_release_tag}" $?
fi


#---------------------------------------------------------------------------------------------------
# teardown
#---------------------------------------------------------------------------------------------------
echo "$(basename $0) success."
exit 0
