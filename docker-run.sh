#!/bin/sh

# 运行容器
docker run -p 8020:8020 -v ./logs:/app/logs -e "TZ=Asia/Shanghai" --name dw-admin  dw-admin:1.0.0
