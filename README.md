# 풀리수학 Android

Freewheelin Inc.((주)프리윌린)가 배포한 풀리수학 Android 앱의 소스 코드입니다.
앱에 포함된 MuPDF가 GNU Affero General Public License v3.0(AGPL-3.0)을 따르므로, MuPDF를
포함해 배포한 버전의 소스를 AGPL-3.0에 따라 공개합니다.

- 각 태그(`vX.Y.Z`)는 Google Play에 같은 이름으로 배포된 버전의 소스입니다.
- 커밋 날짜는 해당 버전의 배포일입니다.

## 빌드

```bash
./gradlew assembleProdDebug
```

- 서버 주소는 배포된 앱과 같은 값이 소스에 그대로 들어 있습니다.
- `app/google-services.json`은 빌드를 위한 플레이스홀더입니다. Firebase(푸시, 분석,
  크래시 리포트)는 동작하지 않습니다. 직접 쓰려면 본인의 Firebase 프로젝트 설정 파일로
  교체하세요.
- 교재 PDF 복호화 키(`publisher_key`)는 포함되어 있지 않습니다.
  `pdf/src/main/res/values/secrets.xml`은 플레이스홀더라 교재 PDF는 열리지 않습니다.
- 배포용 서명 키와 비밀번호는 포함되어 있지 않습니다. `app/build.gradle(.kts)`의
  `signingConfigs`에 있는 `<REDACTED>` 값과 키스토어 경로를 본인의 키로 바꾸면 릴리스
  빌드를 만들 수 있습니다. 디버그 빌드에는 필요하지 않습니다.
- 오래된 태그는 당시의 Gradle, Android Gradle Plugin, JDK를 기준으로 합니다. 지금은
  운영되지 않는 Maven 저장소를 참조하는 등 최신 환경에서는 추가 설정이 필요할 수
  있습니다.

## 기여

이 저장소는 배포한 버전의 소스를 공개하기 위한 것으로, 외부 기여(Pull Request)를 받지
않습니다.

## 라이선스

- 소스 코드: [AGPL-3.0](LICENSE)
- 제3자 구성요소(MuPDF, FreeDrawView, Simple Draw, 글꼴 등): [NOTICE](NOTICE)
- 라이선스 전문: [licenses/](licenses/) (Apache-2.0, GPL-3.0, SIL OFL 1.1)

## 상표

"풀리", "풀리수학", "Pulley", "Pulley Math" 명칭과 로고, 캐릭터, 앱 아이콘 등 브랜드
요소는 Freewheelin Inc.((주)프리윌린)의 상표 및 저작물이며 AGPL-3.0 라이선스의 대상이 아닙니다.
이 소스를 수정하거나 재배포할 때는 해당 명칭과 브랜드 요소를 사용할 수 없으며,
파생 앱은 이를 제거하거나 교체해야 합니다. 자세한 내용은 [NOTICE](NOTICE)를
참고하세요.
