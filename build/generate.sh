#!/bin/bash
#set -eux
#===================================================================================================
#
# Generate Swagger Managed Sources.
#
#===================================================================================================
#---------------------------------------------------------------------------------------------------
# 設定
#---------------------------------------------------------------------------------------------------
dir_script="$(dirname $0)"
cd "$(cd ${dir_script}; cd ..; pwd)" || exit 6

# in-house-swagger インストールディレクトリを指定してください。
#readonly DIR_SWAGGER="/path/to/in-house-swagger-with-depends-0.2.0"
readonly DIR_SWAGGER=""

if [[ "x${DIR_SWAGGER}" = "x" ]]; then
  echo "DIR_SWAGGER が設定されていません。" >&2
  exit 6
fi


#---------------------------------------------------------------------------------------------------
# generate
#---------------------------------------------------------------------------------------------------
readonly DIR_SPECMGR=$(pwd)
readonly GROUP="me.suwash.swagger"
readonly PACKAGE="${GROUP}.spec.manager"

${DIR_SWAGGER}/bin/generate \
  generate \
  -i ${DIR_SPECMGR}/design/swagger.yaml \
  -l spring \
  -o ${DIR_SPECMGR} \
  --group-id "${GROUP}" \
  --artifact-id "swagger-spec-mgr" \
  --invoker-package "${PACKAGE}" \
  --api-package "${PACKAGE}.api.gen" \
  --model-package "${PACKAGE}.model.gen"

exit $?
