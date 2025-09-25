#!/bin/bash

# Protopie Assignment 정지 스크립트

echo "🛑 Protopie Assignment 정지 중..."

# Docker Compose로 모든 서비스 정지
echo "📦 Docker 컨테이너 정지 중..."
docker-compose down

echo "✅ 모든 서비스가 정지되었습니다."
echo ""
echo "💡 데이터를 완전히 삭제하려면:"
echo "   docker-compose down -v"
echo ""
echo "💡 이미지도 삭제하려면:"
echo "   docker-compose down --rmi all -v"

