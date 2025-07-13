# ux-driven-atm

사용자 중심의 경험(UX)을 고려한 웹 기반 ATM 서비스입니다.  
Spring Boot와 JPA를 기반으로 로그인 및 JWT 인증 기능을 구현하였으며,  
계좌 조회, 입금, 출금 기능은 추후 구현을 목표로 설계 중입니다.

🔖 [포트폴리오 제출 버전]
- 버전: [v1.0.0](https://github.com/juyeongMoon888/ux-driven-atm/releases/tag/v1.0.0)
- 설명: JWT 인증 및 Redis 기반 자동 로그아웃 구현을 포함한 최종 제출 버전
- 주요 기능:
  - 회원가입/로그인 API 구현
  - JWT 발급 및 검증
  - Redis를 활용한 리프레시 토큰 및 블랙리스트 관리
  - 로그인 후 사용자 정보 조회 및 세션 유지
    
📌 [커밋 스타일 안내]
- 2025년 7월 10일 부터 커밋 메시지를 한글로 전환합니다. 
- Conventional Commit 스타일(`feat`, `fix`, `chore` 등)은 유지합니다.

🔧 [로컬 환경 설정 안내]
- 본 프로젝트는 민감한 정보 보호 및 환경 분리를 위해 `application.yml` 파일은 Git에 포함되어 있지 않습니다
- 📄 예시 파일: `application-example.yml`
