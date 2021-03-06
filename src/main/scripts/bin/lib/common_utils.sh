#!/bin/bash
#==================================================================================================
#
# 共通関数定義
# ※_で始まるfunctionは、パイプでの呼出しだけを想定しています。
#
#==================================================================================================
#--------------------------------------------------------------------------------------------------
# 実行OS判定
#--------------------------------------------------------------------------------------------------
function is_mac() {
  if [ "$(uname)" == 'Darwin' ]; then
    echo "true"
  else
    echo "false"
  fi
  return 0
}

function is_linux() {
  if [ "$(expr substr $(uname -s) 1 5)" == 'Linux' ]; then
    echo "true"
  else
    echo "false"
  fi
  return 0
}

function is_cygwin() {
  if [ "$(expr substr $(uname -s) 1 10)" == 'MINGW32_NT' ]; then
    echo "true"
  else
    echo "false"
  fi
  return 0
}


#--------------------------------------------------------------------------------------------------
# 文字列操作
#--------------------------------------------------------------------------------------------------
function _trim() {
  cat -                                                                                            | # 標準入力から
  sed -e "s|^  *||g"                                                                               | # leftトリム
  sed -e "s|  *$||g"                                                                                 # rightトリム
  return 0
}

function _ltrim() {
  cat -                                                                                            | # 標準入力から
  sed -e "s|^  *||g"                                                                                 # leftトリム
  return 0
}

function _rtrim() {
  cat -                                                                                            | # 標準入力から
  sed -e "s|  *$||g"                                                                                 # rightトリム
  return 0
}

function _sp_multi2single() {
  cat -                                                                                            | # 標準入力から
  sed -E "s| +| |g"                                                                                  # 複数スペースを単一に置換
  return 0
}


#--------------------------------------------------------------------------------------------------
# 行操作
#--------------------------------------------------------------------------------------------------
function _except_comment_row() {
  cat -                                                                                            | # 標準入力から
  grep -v '^\s*#'                                                                                    # コメント行を除外
  return 0
}

function _except_empty_row() {
  cat -                                                                                            | # 標準入力から
  grep -v '^\s*$'                                                                                    # 空行を除外
  return 0
}


#--------------------------------------------------------------------------------------------------
# SSH
#--------------------------------------------------------------------------------------------------
function gen_ssh_server_key() {
  local _USAGE="Usage: ${FUNCNAME[0]} IP"
  local _PATH_KNOWN_HOSTS=~/.ssh/known_hosts

  local _ip="$1"
  local _ret_code=0

  # 入力チェック
  if [ "${_ip}" = "" ]; then
    echo "IP が指定されていません。" >&2
    echo "${_USAGE}" >&2
    return 1
  fi

  # キーの存在チェック
  cat ${_PATH_KNOWN_HOSTS} | grep ${_ip} > /dev/null 2>&1
  _ret_code=$?
  if [ ${_ret_code} -eq 0 ]; then
    echo "${_ip} のSSHサーバキーは既に存在します。"
    return 0
  fi

  # サーバキー削除
  ssh-keygen -R ${_ip} > /dev/null 2>&1
  _ret_code=$?
  if [ ${_ret_code} -ne 0 ]; then
    echo "${_ip} のSSHサーバキー削除に失敗しました。コマンド: ssh-keygen -R ${_ip}、リターンコード: ${_ret_code}" >&2
    return 1
  fi

  # サーバキー追加
  ssh-keyscan ${_ip} >> ${_PATH_KNOWN_HOSTS} 2> /dev/null
  _ret_code=$?
  if [ ${_ret_code} -ne 0 ]; then
    echo "${_ip} のSSHサーバキー追加に失敗しました。コマンド: ssh-keyscan ${_ip} >> ${_PATH_KNOWN_HOSTS}、リターンコード: ${_ret_code}" >&2
    return 1
  fi

  echo "${_ip} のSSHサーバキーを追加しました。"
  return 0
}


#--------------------------------------------------------------------------------------------------
# 自ホストIPアドレス取得
#--------------------------------------------------------------------------------------------------
function get_ip() {
  # 自IPを標準出力
  echo $(                                                                                            \
    LANG=C /sbin/ifconfig                                                                          | \
    grep "inet addr"                                                                               | \
    grep -v 127.0.0.1                                                                              | \
    head -n 1                                                                                      | \
    awk '{print $2}'                                                                               | \
    cut -d ":" -f 2                                                                                  \
  )
  return 0
}


#--------------------------------------------------------------------------------------------------
# URLエンコード・デコード
#--------------------------------------------------------------------------------------------------
function _urlencode() {
  local _lf='\%0A'

  cat -                                                                                            | # 標準出力から
  python -c 'import sys, urllib ; print urllib.quote(sys.stdin.read());'                           | # URLエンコード
  sed "s|${_lf}$||g"                                                                                 # 末尾に改行コードが付与されるので除外

  return 0
}

function _urldecode() {
  cat -                                                                                            | # 標準出力から
  python -c 'import sys, urllib ; print urllib.unquote(sys.stdin.read());'

  return 0
}


#------------------------------------------------------------------------------
# 拡張子取得
#
# 引数
#   $1: 対象ファイルパス
#------------------------------------------------------------------------------
function get_ext() {
  local _path="$1"
  local _ext="${_path##*.}"

  # 変数展開結果を確認
  if [ "${_ext}" = "gz" ]; then
    # gzの場合、2重拡張子を確認 ※tar.gzのみ対応
    if [ "$(basename ${_path} .tar.gz)" != "$(basename ${_path})" ]; then
      _ext="tar.gz"
    fi

  elif [ "${_ext}" = "${_path}" ]; then
    # pathそのままの場合、拡張子なし
    _ext=""
  fi

  echo "${_ext}"
  return ${EXITCODE_SUCCESS}
}


#--------------------------------------------------------------------------------------------------
# 暗号化・復号化
#--------------------------------------------------------------------------------------------------
function gen_encrypt_key() {
  # 設定チェック
  if [ "${PATH_ENCRYPT_KEY}" = "" ]; then
    echo "PATH_ENCRYPT_KEY が設定されていません。" >&2
    return 1
  fi
  if [ "${PATH_DECRYPT_KEY}" = "" ]; then
    echo "PATH_DECRYPT_KEY が設定されていません。" >&2
    return 1
  fi

  # ディレクトリ作成
  local dir_encrypt="$(dirname ${PATH_ENCRYPT_KEY})"
  if [ ! -d "${dir_encrypt}" ]; then
    mkdir -p "${dir_encrypt}"
  fi
  local dir_decrypt="$(dirname ${PATH_DECRYPT_KEY})"
  if [ ! -d "${dir_decrypt}" ]; then
    mkdir -p "${dir_decrypt}"
  fi

  # 鍵作成
  echo "openssl req -x509 -nodes -newkey rsa:2048 -keyout \"${PATH_DECRYPT_KEY}\" -out \"${PATH_ENCRYPT_KEY}\" -subj '/'"
  openssl req -x509 -nodes -newkey rsa:2048 -keyout "${PATH_DECRYPT_KEY}" -out "${PATH_ENCRYPT_KEY}" -subj '/'
  return $?
}

function _encrypt() {
  # 設定チェック
  if [ "${PATH_ENCRYPT_KEY}" = "" ]; then
    echo "PATH_ENCRYPT_KEY が設定されていません。" >&2
    return 1
  fi
  if [ ! -f "${PATH_ENCRYPT_KEY}" ]; then
    echo "${PATH_ENCRYPT_KEY} が存在しません。" >&2
    return 1
  fi

  # 暗号化
  cat -                                                                                            | # 標準入力（平文）を
  openssl smime -encrypt -aes256 -binary -outform PEM "${PATH_ENCRYPT_KEY}"                          # PATH_ENCRYPT_KEYで暗号化
  return 0
}

function _decrypt() {
  # 設定チェック
  if [ "${PATH_DECRYPT_KEY}" = "" ]; then
    echo "PATH_DECRYPT_KEY が設定されていません。" >&2
    return 1
  fi
  if [ ! -f "${PATH_DECRYPT_KEY}" ]; then
    echo "${PATH_DECRYPT_KEY} が存在しません。" >&2
    return 1
  fi

  # 復号化
  cat -                                                                                            | # 標準入力（暗号化文字列）を
  openssl smime -decrypt -binary -inform PEM -inkey "${PATH_DECRYPT_KEY}"                            # PATH_DECRYPT_KEYで復号化
  return 0
}


#--------------------------------------------------------------------------------------------------
# setオプション判定
#--------------------------------------------------------------------------------------------------
function is_errorexit_on() {
  is_setoption_on "errexit"
}
function is_nounset_on() {
  is_setoption_on "nounset"
}
function is_xtrace_on() {
  is_setoption_on "xtrace"
}
function is_setoption_on() {
  local _target="$1"
  set -o                                                                                           |
  grep "${_target}"                                                                                |
  tr '\t' ' '                                                                                      |
  sed -E "s| +| |g"                                                                                |
  cut -d ' ' -f 2                                                                                  |
  sed -e 's|on|true|'                                                                              |
  sed -e 's|off|false|'
}
# SAMPLE
#local _before_set_errorexit=$(is_errorexit_on)
#local _before_set_nounset=$(is_nounset_on)
#local _before_set_xtrace=$(is_xtrace_on)
#set +eux
#
# 任意の処理...
#
#if [ "${_before_set_errorexit}" = "true" ]; then
#  set -e
#fi
#if [ "${_before_set_nounset}" = "true" ]; then
#  set -u
#fi
#if [ "${_before_set_xtrace}" = "true" ]; then
#  set -x
#fi
