#!/bin/bash

# 脚本功能：停止运行中的Spring Boot项目

echo "正在查找运行中的Spring Boot应用..."

# 查找包含java-demo的Java进程
PIDS=$(ps -ef | grep "dw-admin" | grep -v "grep" | awk '{print $2}')

if [ -z "$PIDS" ]; then
    echo "未找到运行中的Spring Boot应用"
    exit 0
fi

echo "找到以下进程ID: $PIDS"

# 逐个终止进程
for PID in $PIDS; do
    echo "正在终止进程 $PID..."
    kill $PID
    
    # 等待进程结束
    sleep 2
    
    # 检查进程是否仍然存在
    if ps -p $PID > /dev/null; then
        echo "进程 $PID 未正常终止，强制终止..."
        kill -9 $PID
    else
        echo "进程 $PID 已成功终止"
    fi
done

echo "Spring Boot应用已停止"