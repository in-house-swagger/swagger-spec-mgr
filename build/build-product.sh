#!/bin/bash
#set -eux
#===================================================================================================
#
# Production Build
#
#===================================================================================================
#---------------------------------------------------------------------------------------------------
# 設定
#---------------------------------------------------------------------------------------------------
dir_script="$(dirname $0)"
cd "$(cd ${dir_script}; cd ..; pwd)" || exit 6

readonly BUILD_PROFILE="product"

readonly DIR_BASE="$(pwd)"
readonly DIR_WORK="${DIR_BASE}/target"
readonly DIR_DIST="${DIR_BASE}/dist"

# プロダクト名
product_name="$(basename ${DIR_BASE})"
# バージョン
# TODO xmlパーサでxpath指定で取得する？
version=$(                                                                                         \
  cat pom.xml                                                                                      |
  grep "^    <version"                                                                             |
  sed -e "s|^  *||g"                                                                               |
  sed -e "s|<version>||"                                                                           |
  sed -e "s|</version>||"                                                                          \
)
if [[ "x${version}" = "x" ]]; then
  echo "pom.xmlからバージョンが取得できません。" >&2
  exit 6
fi

#---------------------------------------------------------------------------------------------------
# build
#---------------------------------------------------------------------------------------------------
echo "出力ディレクトリのクリア"
if [[ -d "${DIR_DIST}" ]]; then
  rm -fr "${DIR_DIST}"
fi
mkdir -p "${DIR_DIST}"

echo ""
echo "ビルドスクリプトの実行"
mvn clean package -P ${BUILD_PROFILE}
retcode=$?
if [[ ${retcode} -ne 0 ]]; then
  echo "ビルドスクリプトでエラーが発生しました。" >&2
  exit 6
fi


#---------------------------------------------------------------------------------------------------
# 配布アーカイブ作成
#---------------------------------------------------------------------------------------------------
echo ""
echo "配布アーカイブの収集"
mv "${DIR_WORK}/${product_name}_${version}_${BUILD_PROFILE}.tar.gz" "${DIR_DIST}/"
retcode=$?
if [[ ${retcode} -ne 0 ]]; then
  echo "配布アーカイブの収集でエラーが発生しました。" >&2
  exit 6
fi

echo ""
echo "ビルドが完了しました。"
exit 0
