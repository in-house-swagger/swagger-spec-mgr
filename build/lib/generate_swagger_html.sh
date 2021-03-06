#!/bin/bash
#set -eux
#===================================================================================================
#
# Generate api manual html
#
#===================================================================================================
#---------------------------------------------------------------------------------------------------
# 設定
#---------------------------------------------------------------------------------------------------
dir_script="$(dirname $0)"
cd "$(cd ${dir_script}; cd ../..; pwd)" || exit 6

readonly SCRIPT_NAME="$(basename $0 .sh)"
readonly DIR_BASE="$(pwd)"
readonly DIR_WORK="${DIR_BASE}/target/${SCRIPT_NAME}"
readonly DIR_DIST="${DIR_BASE}/docs/manual/webapi"


#---------------------------------------------------------------------------------------------------
# 引数
#---------------------------------------------------------------------------------------------------
# なし


#---------------------------------------------------------------------------------------------------
# 事前処理
#---------------------------------------------------------------------------------------------------
echo "${SCRIPT_NAME}"

echo "-- 作業ディレクトリの初期化"
if [[ -d "${DIR_WORK}" ]]; then
  rm -fr "${DIR_WORK}"
fi
mkdir -p "${DIR_WORK}"

echo "-- 出力ディレクトリの初期化"
if [[ -d "${DIR_DIST}" ]]; then
  rm -fr "${DIR_DIST}"
fi
mkdir -p "${DIR_DIST}"


#---------------------------------------------------------------------------------------------------
# 本処理
#---------------------------------------------------------------------------------------------------
echo "-- adoc 生成"
java -jar ${DIR_BASE}/build/lib/swagger2markup-cli-1.3.1.jar convert                               \
  -i ${DIR_BASE}/docs/design/webapi/swagger.yaml                                                   \
  -d ${DIR_WORK}

echo "-- index.adoc 生成"
{
  echo "include::overview.adoc[]"
  echo "include::paths.adoc[]"
  echo "include::security.adoc[]"
  echo "include::definitions.adoc[]"
} > ${DIR_WORK}/index.adoc


echo "-- html 生成"
cd ${DIR_WORK}
asciidoctor -a toc=left index.adoc
cd - > /dev/null
mv ${DIR_WORK}/*.html ${DIR_DIST}/


#---------------------------------------------------------------------------------------------------
# 事後処理
#---------------------------------------------------------------------------------------------------
exit 0
