#!/bin/sh

# 定义变量
JAR_FILE=$(find . -name "*.jar" -print)
PID_FILE=pid.file
RUNNING=N

# 定义函数
start() {
  if [ "${RUNNING}" = "Y" ]; then
    echo "服务已处于启动状态"
  elif [ -z "${JAR_FILE}" ]; then
    echo "默认: 未找到文件后缀为 .jar 的应用程序"
  else
    nohup java -Xmx1024m \
      -Djava.security.egd=file:/dev/./urandom \
      -jar "${JAR_FILE}" \
      >nohup.out 2>&1 &
    echo $! >"${PID_FILE}"
    echo "服务 ${JAR_FILE} 启动中..."
    tail -f nohup.out
  fi
}

stop() {
  if [ "${RUNNING}" = "Y" ]; then
    kill -9 "${PID}" 2>/dev/null
    rm -f "${PID_FILE}"
    RUNNING=N
    echo "服务已关闭"
  else
    echo "服务未启动, 无需关闭"
  fi
}

restart() {
  stop
  start
}

# 主入口
if [ -f "${PID_FILE}" ]; then
  PID=$(cat "${PID_FILE}")
  if [ -n "${PID}" ] && kill -0 "${PID}" 2>/dev/null; then
    RUNNING=Y
  fi
fi

case "$1" in
start)
  start
  ;;
stop)
  stop
  ;;
restart)
  restart
  ;;
*)
  echo "使用方式: $0 { start | stop | restart }"
  echo "  - 启动服务: $0 start"
  echo "  - 关闭服务: $0 stop"
  echo "  - 重启服务: $0 restart"
  exit 1
  ;;
esac
