# 아메시 터미널

Work In Progress (80%)

TODO

- 콘솔 패널 탭컴플리터 구현
- 파일 탐색기 업로드, 다운로드 안정화
- 플러그인 웹소켓 안정화
- 계정에 터미널 권한 부여 구현
- 소유자 계정 외 추가 계정에 가변 터미널 권한 부여 구현
- 터미널 목록 페이지 꾸미기
- 아메시 메인페이지에 터미널 같이 보여주기
- 사용자 가이드 작성

---

**마인크래프트 서버를 위한 웹 기반 원격 터미널.**

<br>

아메시 터미널로 가능한 일...

> _서버 메모리, 프로세서 상태 확인, 접속 플레이어, 월드 확인<br>
> 웹 기반의 원격 서버 콘솔 사용<br>
> 간단한 파일 탐색기 & 텍스트 편집기<br>
> 플레이어 추적 & 플레이어 상태 모니터링<br>
> 월드 설정 관리 & 월드 상태 모니터링_

<br>

이 아메시 터미널 노드 플러그인은 버킷 기반의 서버에서 동작합니다. `크래프트버킷`, `스피곳`, `페이퍼` 등의 서버 프로그램과 호환됩니다. 다른 서버 프로그램을 위한 아메시 터미널이 필요한 경우 아래의 링크를 확인해보세요.

- [`번지코드 서버용 아메시 터미널`](https://github.com/wnynya/Amethy-Terminal-Bungeecord)

아메시 터미널은 1.14.\* - 1.19.\* 버전의 마인크래프트 서버와 호환됩니다.<br>
아메시 터미널 플러그인을 사용하려면 자바 버전 11 이상의 환경에서 서버를 실행해야 합니다.

# 설치와 기본 설정

1. 아래의 링크 혹은 [`아메시 홈페이지`](https://amethy.wany.io) 에서 플러그인의 최신 버전을 다운로드합니다.<br>
   [`Amethy-Terminal-Bukkit.jar (클릭하여 다운로드)`](https://api.wany.io/amethy/repository/Amethy-Terminal-Bukkit/release/latest/Amethy-Terminal-Bukkit.jar)

2. `Amethy-Terminal-Bukkit.jar` 파일을 마인크래프트 서버의 `plugins` 디렉터리에 넣습니다.

3. 서버를 재시작, 리로드하거나 [`Plugman`](https://www.spigotmc.org/resources/plugmanx.88135) 등의 플러그인을 이용해 아메시 터미널 플러그인을 로드합니다.

4. 서버 콘솔에 `aterm grant <와니네 계정 ID>` 명령어를 입력하여 서버 접근 권한을 부여합니다.

5. [`Amethy Terminal 웹 사이트`](https://amethy.wany.io/terminal)를 방문하여 웹 터미널을 사용합니다.

# 웹 터미널 사용

## 대시보드 패널

## 콘솔 패널

## 파일 탐색기 패널

## 플레이어 패널

## 월드 패널

## 설정 패널

# 명령어

## 터미널 권한 부여

`/aterm grant <와니네 계정 ID>`

특정 와니네 계정에 터미널 노드에 대한 접근 권한을 부여합니다. 이 명령어는 콘솔에서만 사용할 수 있습니다. 한번 접근 권한을 부여한 터미널 노드는 웹 페이지에서 터미널 노드를 제거하거나 서버 디렉토리의 `terminal.json` 파일을 삭제하기 전에는 다른 와니네 계정에 접근 권한을 부여할 수 없습니다.

## 플러그인 버전 확인

`/aterm version`

## 플러그인 업데이트

`/aterm update`

## 플러그인 업데이터 설정 확인 및 변경

`/aterm updater (channel|automation) [args...]`

- 현재 업데이터 채널 확인

  `/aterm updater channel`

- 업데이터 채널을 release / dev 로 변경

  `/aterm updater channel (release|dev)`

- 현재 자동 업데이트 활성화 여부 확인

  `/aterm updater automation`

- 자동 업데이트 활성화 / 비활성화

  `/aterm updater automation (enable|disable)`

## 플러그인 리로드

`/aterm reload`
