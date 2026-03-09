# Trollo Backend Deployment Guide

## GitHub Secrets (Repository Settings -> Secrets and variables -> Actions)
이 프로젝트는 GitHub Actions를 이용해 운영 서버(Oracle Cloud)에 배포됩니다.
아래 환경변수들을 Github 저장소의 Secrets에 등록해주세요:

- `ORACLE_HOST`: 오라클 인스턴스의 공인 IP
- `ORACLE_USERNAME`: 오라클 인스턴스의 접속 유저네임 (예: `ubuntu` 또는 `opc`)
- `ORACLE_SSH_KEY`: 서버에 접근할 수 있는 SSH Private Key 내용 전체
- `DB_URL`: 운영환경 데이터베이스 접속 URL (예: `jdbc:mysql://{DB_HOST}:3306/trollo?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true&characterEncoding=UTF-8`)
- `DB_USERNAME`: 운영 Database 접속 유저네임 (예: `admin`)
- `DB_PASSWORD`: 운영 Database 접속 비밀번호
- `JWT_SECRET`: JWT 토큰 암호화에 사용할 32자 이상의 무작위 문자열 비밀키
