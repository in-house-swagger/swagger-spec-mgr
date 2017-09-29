#!/bin/bash
set -eux
#===================================================================================================
#
# Generate swagger.yaml
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
readonly DIR_DIST="${DIR_BASE}/docs/design/webapi"


#---------------------------------------------------------------------------------------------------
# 引数
#---------------------------------------------------------------------------------------------------
# version
readonly version="$1"


#---------------------------------------------------------------------------------------------------
# 事前処理
#---------------------------------------------------------------------------------------------------
echo "${SCRIPT_NAME}"

echo "-- 作業ディレクトリの初期化"
if [[ -d "${DIR_WORK}" ]]; then
  rm -fr "${DIR_WORK}"
fi
mkdir -p "${DIR_WORK}"

echo "-- デプロイ"
cd ${DIR_BASE}/dist/
tar xzf ./swagger-spec-mgr_${version}_product.tar.gz


echo "-- アプリ起動"
${DIR_BASE}/dist/swagger-spec-mgr_${version}/bin/server start


#---------------------------------------------------------------------------------------------------
# 本処理
#---------------------------------------------------------------------------------------------------
echo "-- swagger.json 生成"
curl -X GET                                  \
  --output ${DIR_WORK}/swagger.json \
  "http://localhost:8081/v1/api-docs"


echo "-- デフォルトユーザ追加"
curl -X POST                                \
  --header 'Content-Type: application/json' \
  --header 'Accept: application/json'       \
  "http://localhost:8081/v1/users"


echo "-- swagger.yaml生成"
curl -X POST                                \
  --header 'Content-Type: application/json' \
  --header 'Accept: application/x-yaml'     \
  --data @${DIR_WORK}/swagger.json \
  --output ${DIR_DIST}/swagger.yaml       \
  "http://localhost:8081/v1/specs/spec-mgr"


#---------------------------------------------------------------------------------------------------
# 事後処理
#---------------------------------------------------------------------------------------------------
echo "-- アプリ停止"
${DIR_BASE}/dist/swagger-spec-mgr_${version}/bin/server stop

exit 0
