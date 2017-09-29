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

readonly SONAR_URL="https://sonarcloud.io"
readonly SONAR_ORGANIZATION="suwa-sh-github"
readonly SONAR_EXCLUDES="src/test/**,src/main/java/io/**,**/gen/**,**/MessageConst.java,**/*Exception.java"

readonly DIR_BASE="$(pwd)"
readonly DIR_WORK="${DIR_BASE}/target"
readonly DIR_DIST="${DIR_BASE}/dist"

readonly URL_BASE="http://localhost:8081/v1"

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

echo "ビルドスクリプトの実行"
if [[ "${SONAR_TOKEN}x" = "x" ]]; then
  echo "SONAR_TOKEN が定義されていません。sonar解析をスキップします。"
  mvn clean package                                                                                \
    -P ${BUILD_PROFILE}                                                                            \

else
  mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar                       \
    -P ${BUILD_PROFILE}                                                                            \
    -Dsonar.host.url=${SONAR_URL}                                                                  \
    -Dsonar.organization=${SONAR_ORGANIZATION}                                                     \
    -Dsonar.login=${SONAR_TOKEN}                                                                   \
    -Dsonar.exclusions="${SONAR_EXCLUDES}"
fi
retcode=$?
if [[ ${retcode} -ne 0 ]]; then
  echo "ビルドスクリプトでエラーが発生しました。" >&2
  exit 6
fi


#---------------------------------------------------------------------------------------------------
# 配布アーカイブ作成
#---------------------------------------------------------------------------------------------------
echo "配布アーカイブの収集"
mv "${DIR_WORK}/${product_name}_${version}_${BUILD_PROFILE}.tar.gz" "${DIR_DIST}/"
retcode=$?
if [[ ${retcode} -ne 0 ]]; then
  echo "配布アーカイブの収集でエラーが発生しました。" >&2
  exit 6
fi


#---------------------------------------------------------------------------------------------------
# swagger.yamlの生成
#---------------------------------------------------------------------------------------------------
echo ""
${DIR_BASE}/build/lib/generate_swagger_yaml.sh "${version}" "${URL_BASE}"
retcode=$?
if [[ ${retcode} -ne 0 ]]; then
  echo "swagger.yamlの生成でエラーが発生しました。" >&2
  exit 6
fi


#---------------------------------------------------------------------------------------------------
# webapi manualの生成
#---------------------------------------------------------------------------------------------------
echo ""
${DIR_BASE}/build/lib/generate_swagger_html.sh
retcode=$?
if [[ ${retcode} -ne 0 ]]; then
  echo "webapi manualの生成でエラーが発生しました。" >&2
  exit 6
fi

echo ""
echo "ビルドが完了しました。"
exit 0
