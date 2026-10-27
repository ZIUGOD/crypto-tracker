# Lembrete: onde atualizar quando mexer em algo

Lista de conferência. Nem tudo se aplica a toda mudança: procure o assunto, marque o que couber e ignore o resto.

## Índice

[Sempre](#sempre) · [Textos da interface](#textos-da-interface) · [Preços e provedores](#preços-e-provedores-de-api) · [Notificações](#notificações) · [Login](#login-com-google) · [Anúncios](#anúncios-admob) · [Páginas legais](#páginas-legais-github-pages) · [Loja](#loja-play-console) · [Nova versão](#nova-versão-e-publicação) · [Identidade visual](#identidade-visual) · [Novo idioma](#novo-idioma) · [Segredos](#segredos-nunca-no-git)

## Sempre

- [ ] `./gradlew :app:testDebugUnitTest :app:lintDebug` passando.
- [ ] Valores ajustáveis em `config/AppConfig.kt`, não espalhados pelo código.
- [ ] `DEVELOPMENT.md`, se mudou estrutura, fluxo ou convenção.

## Textos da interface

- [ ] `res/values/strings.xml` (inglês) **e** `res/values-pt-rBR/strings.xml` (português). O `StringResourcesTest` falha se as chaves ou os `%s`/`%d` não baterem.
- [ ] Subtítulos e descrições das telas (ex.: `settings_subtitle`), se a função mudou.
- [ ] Capturas de tela da loja, se a interface mudou (ver [Loja](#loja-play-console)).

## Preços e provedores de API

- [ ] `AppConfig.Network` (URLs) e `AppConfig.RateLimit` (intervalos e backoff).
- [ ] `data/repository`: `CryptoRepository`, `CoinParser`, `ProviderGuard`.
- [ ] Testes: `CryptoRepositoryTest`, `CoinParserTest`.
- [ ] Política de Privacidade e Termos (`docs/`): lista de provedores e domínios.
- [ ] `PLAY_RELEASE.md` (seção "Referência técnica") e `PLAY_LISTING.md` ("Fontes dos dados").
- [ ] Os ids das moedas devem continuar iguais aos do CoinGecko (moedas fixadas e descrições dependem disso).

## Notificações

- [ ] `notification/CryptoNotificationManager.kt`.
- [ ] `AppConfig.Notifications` (`CHANNEL_ID`, limite de moedas fixadas, ids).
- [ ] A importância de um canal **não muda depois de criado**: troque o `CHANNEL_ID`.
- [ ] Ícone pequeno: `res/drawable/ic_notification.xml` (só branco; o Android pinta).
- [ ] Intervalo em segundo plano: `AppConfig.BACKGROUND_SYNC_INTERVAL_MINUTES`.
- [ ] Textos da loja que citam intervalos e o limite de 5 moedas (`PLAY_LISTING.md`).
- [ ] Strings do canal (`notification_channel_name` e `_description`) em inglês e português.

## Login com Google

- [ ] `AppConfig.Account.LOGIN_ENABLED` (hoje `false`).
- [ ] Se ligar: Política de Privacidade (dados da conta), Termos, formulário "Segurança dos dados" da Play Console e a ficha da loja.
- [ ] Cliente OAuth e `google_web_client_id` (`resValue` em `app/build.gradle.kts`).

## Anúncios (AdMob)

- [ ] `app/build.gradle.kts`: debug usa os ids de teste do Google; release usa os ids reais.
- [ ] Nunca clicar nos próprios anúncios no build de release.
- [ ] Política de Privacidade, Termos e "Segurança dos dados", se mudar o SDK.
- [ ] Para distribuir na Europa ou no Reino Unido: consentimento UMP antes.

## Páginas legais (GitHub Pages)

Ficam em `docs/` na branch `master`.

- [ ] `docs/privacy-policy` e `docs/terms-of-use` **e** as versões `docs/pt-br/...` (inglês e português dizendo a mesma coisa).
- [ ] `docs/index.html` e `docs/404.html` (links absolutos).
- [ ] Data de vigência nas 4 páginas.
- [ ] `AppConfig.Legal` (URLs) e `LegalPagesTest`, se mudar o endereço.
- [ ] Link da política na Play Console e em `PLAY_LISTING.md`.
- [ ] O repositório precisa continuar **público** para o Pages funcionar.
- [ ] Se mudar nome, e-mail de contato ou dados coletados, revisar tudo acima.

## Loja (Play Console)

- [ ] `PLAY_LISTING.md`: nome, descrição curta e completa (en e pt-BR).
- [ ] Capturas, ícone 512 e imagem de destaque em `C:\Users\caiqu\Pictures\crypto-tracker-play\`.
- [ ] Formulário "Segurança dos dados" e declaração de anúncios.
- [ ] Países de distribuição (hoje só Brasil).
- [ ] Notas da versão.

## Nova versão e publicação

- [ ] `app/build.gradle.kts`: `versionCode` (+1 a cada envio) e `versionName`.
- [ ] `keystore.properties` e o `.jks` existem (nunca no Git; com backup).
- [ ] `./gradlew :app:bundleRelease` (envio) e `:app:assembleRelease` (teste).
- [ ] Testar o APK assinado em aparelho real (checklist no `PLAY_RELEASE.md`).
- [ ] `PLAY_RELEASE.md`: tabela de situação e data da última revisão.
- [ ] Merge `dev` → `master`, com commits usando o e-mail noreply do GitHub.

## Identidade visual

- [ ] Logo: `res/drawable-nodpi/app_logo.png` (usado no ícone do app e na loja).
- [ ] Ícone do app: `res/mipmap-anydpi/ic_launcher*.xml`.
- [ ] Ícone da notificação: `res/drawable/ic_notification.xml`.
- [ ] Ícone 512 e imagens de destaque (gerados a partir do logo).

## Novo idioma

- [ ] `AppLocale`, nova pasta `res/values-XX/strings.xml` e o seletor em Configurações.
- [ ] Páginas legais, ficha da loja e capturas no novo idioma.

## Segredos (nunca no Git)

`keystore.properties`, `*.jks`, `*.keystore`, `local.properties`, senhas e chaves.

Os ids do AdMob e o client id OAuth web são públicos por natureza e podem ficar no repositório.
