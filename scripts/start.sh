#!/bin/bash

# Protopie Assignment 시작 스크립트

echo "🚀 Protopie Assignment 시작 중..."

# Docker Compose로 모든 서비스 시작
echo "📦 Docker 컨테이너 시작 중..."
docker-compose up -d

# 서비스 상태 확인
echo "⏳ 서비스 시작 대기 중..."
sleep 10

# 헬스체크
echo "🔍 서비스 상태 확인 중..."

# PostgreSQL 헬스체크
if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo "✅ PostgreSQL: 정상"
else
    echo "❌ PostgreSQL: 오류"
fi

# Redis 헬스체크
if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis: 정상"
else
    echo "❌ Redis: 오류"
fi

# RabbitMQ 헬스체크
if docker-compose exec -T rabbitmq rabbitmq-diagnostics ping > /dev/null 2>&1; then
    echo "✅ RabbitMQ: 정상"
else
    echo "❌ RabbitMQ: 오류"
fi

# 애플리케이션 시작 대기
echo "⏳ 애플리케이션 시작 대기 중..."
#sleep 60

echo ""
echo "🎉 모든 서비스가 정상적으로 시작되었습니다!"
echo ""
echo "📋 서비스 정보:"
echo "  - 애플리케이션: http://localhost:8080"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
echo "  - RabbitMQ Management: http://localhost:15672 (admin/admin123)"
echo ""
echo "🔑 기본 계정:"
echo "  - 관리자: admin@protopie.com / admin123!"
echo "  - 사용자: user@protopie.com / user123!"
echo ""
echo "💡 애플리케이션 로그 확인: docker-compose logs app"

