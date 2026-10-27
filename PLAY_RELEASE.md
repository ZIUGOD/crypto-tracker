# Crypto Tracker - Guia de publicação na Play Store

Última revisão: 2026-10-27 (código em `master`, commit `3b4b321` ou posterior).

## Situação atual

| Item | Estado |
|---|---|
| Código, testes (66) e lint (0 erros) | Pronto |
| Build de release (R8 + IDs de produção do AdMob) | Compila; **ainda não testado em aparelho** |
| Páginas legais no GitHub Pages | No ar (HTTP 200): [Política](https://ziugod.github.io/crypto-tracker/privacy-policy/) · [Termos](https://ziugod.github.io/crypto-tracker/terms-of-use/) |
| Login com Google | Desligado (`AppConfig.Account.LOGIN_ENABLED = false`) |
| Provedores de preço | CoinGecko + plano B CoinPaprika, com proteção contra excesso de requisições |
| Chave de assinatura | **Falta gerar** (passo 1) |
| Teste do release em aparelho real | **Falta** (passo 4) |
| Conta e ficha na Play Console | **Falta** (passos 5 a 7) |

Os passos abaixo estão na ordem em que devem ser feitos.

## 1. Chave de assinatura (uma vez só)
O app usa o **Play App Signing**: você assina o envio com uma *chave de upload* e o Google guarda a chave final de distribuição.

1. Gere a chave e guarde o arquivo e as senhas fora do Git e fora da pasta do projeto:
   ```
   keytool -genkeypair -v -keystore C:\Users\caiqu\keys\crypto-tracker-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Crie `keystore.properties` na raiz do projeto. O `.gitignore` já bloqueia esse arquivo e também `*.jks` e `*.keystore`:
   ```
   storeFile=C:/Users/caiqu/keys/crypto-tracker-upload.jks
   storePassword=SUA_SENHA
   keyAlias=upload
   keyPassword=SUA_SENHA
   ```
3. Faça **backup do `.jks` e das senhas** em dois lugares (por exemplo, gerenciador de senhas e um pendrive). Perder a chave de upload é recuperável pelo suporte do Google, mas demora.

Sem o `keystore.properties` o build continua funcionando, só que sai sem assinatura.

## 2. Versão
Em `app/build.gradle.kts`, `versionCode` (inteiro, **sobe a cada envio**) e `versionName` (texto mostrado ao usuário). Hoje: `1` e `"1.0"`.

## 3. Gerar os pacotes
```
./gradlew :app:testDebugUnitTest :app:lintDebug   # confira que passa
./gradlew :app:bundleRelease                      # para a Play Console
./gradlew :app:assembleRelease                    # APK assinado, para testar no aparelho
```
- AAB (envio): `app/build/outputs/bundle/release/app-release.aab`
- APK (teste): `app/build/outputs/apk/release/app-release.apk` (sem o sufixo `-unsigned` quando a chave está configurada)

## 4. Teste do build de release em aparelho real
O emulador só validou o build de **debug**. O release usa o R8 (minificação) e já teve um crash no passado, então teste o APK assinado no seu celular:
```
adb install -r app/build/outputs/apk/release/app-release.apk
```
- [ ] Abre sem travar.
- [ ] A lista das 10 moedas carrega, com logos, e o botão de tentar novamente funciona.
- [ ] Modo avião: aparece a lista salva com o aviso de "dados salvos", sem texto de erro técnico.
- [ ] Fixar uma moeda e receber a notificação (aceite a permissão no Android 13+).
- [ ] Tema claro, escuro e automático.
- [ ] Idioma: inglês e português; a tela reinicia sem perder a aba.
- [ ] Configurações: sem seção de conta; os links Política e Termos abrem as páginas.
- [ ] O banner de anúncio aparece. **Este build usa os IDs reais do AdMob: não clique nos seus próprios anúncios** (isso pode suspender a conta). Se o anúncio não aparecer logo na primeira vez, é normal: contas novas podem demorar horas para começar a receber anúncios.
- [ ] Tela pequena e fonte grande (escala 1,3x): nada cortado.
- [ ] Android 11 ou inferior (sem desfoque real): legível.

Para ver o app em ação sem gerar impressões reais, use o build de debug (`:app:installDebug`), que usa os anúncios de teste do Google.

## 5. Conta de desenvolvedor
- Conta na Play Console (taxa única de cadastro) com verificação de identidade.
- **Contas pessoais novas costumam precisar de um teste fechado com um número mínimo de testadores por algum tempo antes de liberar a produção.** Confira o requisito atual em *Painel → Requisitos de produção*; isso pode atrasar o lançamento em semanas, então comece cedo.
- AdMob: tenha a conta ativa, com dados de pagamento e impostos preenchidos, e vincule o app à ficha da Play Store depois da publicação.

## 6. Ficha e declarações na Play Console

### Criar o app
Nome: Crypto Tracker. Idioma padrão e demais: inglês e português (Brasil). App gratuito, "contém anúncios".

### Política de privacidade
*Conteúdo do app → Política de Privacidade*: `https://ziugod.github.io/crypto-tracker/privacy-policy/` (a página em português é `…/pt-br/privacy-policy/`).

### Anúncios
*Conteúdo do app → Anúncios*: **Sim, o app contém anúncios** (AdMob).

### Segurança dos dados
O app não tem conta e não envia dados para servidor próprio. Quem coleta é o SDK do AdMob.
- Coleta ou compartilha dados? **Sim** (por causa do SDK de anúncios).
- Tipos a declarar, conforme o guia oficial do AdMob ("Declarar dados do SDK de anúncios"): **ID do dispositivo ou outros IDs** (ID de publicidade) e os dados de **desempenho e diagnóstico**; confira no guia atual se ele pede também **localização aproximada**.
- Finalidade: **Publicidade ou marketing** e **Análise**.
- Dados criptografados em trânsito: **Sim** (HTTPS).
- Nome, e-mail, localização precisa, contatos, fotos etc.: **não coletados**.
- Pedido de exclusão de dados: o app não mantém conta; informe isso e aponte o e-mail de contato da política.

Confira o texto do guia do AdMob na hora de preencher, porque o Google atualiza a lista.

### Outras declarações
- Público-alvo: **maiores de 13 anos** (ou 18+); o app não é para crianças.
- Classificação de conteúdo: questionário (app informativo, sem violência etc.).
- Categoria: Finanças. O app **não** é corretora, não negocia nem dá conselho: a descrição deve dizer que é só informativo.
- Países: **Brasil** por enquanto. Sem tela de consentimento de anúncios (UMP), não distribua para o Espaço Econômico Europeu nem para o Reino Unido.

### Materiais da ficha
- Ícone 512×512 px, imagem de destaque 1024×500 px e pelo menos 2 capturas de tela de celular (use o emulador ou o aparelho).
- Descrição curta (até 80 caracteres) e completa, em inglês e português, deixando claro que os preços vêm de terceiros e que o app não dá conselho financeiro.
- E-mail de contato: o mesmo da política de privacidade.

## 7. Enviar
1. *Testes → Teste interno* (ou fechado, se a conta exigir): envie o `.aab`, adicione testadores e instale pela loja.
2. Corrija o que aparecer, aumente o `versionCode` e envie de novo quando necessário.
3. *Produção*: crie a versão, envie o `.aab`, preencha as notas da versão e envie para análise. A primeira análise costuma levar alguns dias.

## 8. Depois de publicar
- Acompanhe *Android vitals* (travamentos e ANRs) e as avaliações.
- Mantenha as páginas legais no ar e o repositório **público** (o GitHub Pages gratuito não funciona em repositório privado). Se mudar o endereço, atualize `AppConfig.Legal`, o `docs/` e a ficha da loja.
- Se um dia ligar o login (`LOGIN_ENABLED = true`), a política de privacidade precisa voltar a descrever os dados da conta e o formulário "Segurança dos dados" precisa ser atualizado.
- Para distribuir na Europa, adicione primeiro o consentimento de anúncios (UMP).

## Referência técnica: preços e proteção contra excesso de requisições
- Principal: CoinGecko. Plano B: CoinPaprika (gratuita, sem chave). A CoinCap v2 foi desativada e removida.
- Proteção (valores em `AppConfig.RateLimit`): uma requisição por vez; resultado reutilizado por 15 s; provedor que falha (HTTP 429/5xx, timeout, resposta inválida) fica de fora por 30 s, dobrando até 10 min, ou pelo `Retry-After` do servidor. Sem internet não há penalidade.
- Se os dois provedores ficarem indisponíveis, o app mostra os dados salvos.
