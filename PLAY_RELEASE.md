# Crypto Tracker - Publicação na Play Store

Páginas legais (GitHub Pages, branch `master`, pasta `/docs`):
- Política de Privacidade: https://ziugod.github.io/crypto-tracker/privacy-policy/
- Termos de Uso: https://ziugod.github.io/crypto-tracker/terms-of-use/

## 1. Chave de assinatura (uma vez só)
O app usa o **Play App Signing**: você assina o envio com uma *chave de upload* e o Google guarda a chave final.

1. Gere a chave (guarde o arquivo e as senhas em um lugar seguro, fora do Git):
   ```
   keytool -genkeypair -v -keystore C:\Users\caiqu\keys\crypto-tracker-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Crie `keystore.properties` na raiz do projeto (já está no `.gitignore`):
   ```
   storeFile=C:/Users/caiqu/keys/crypto-tracker-upload.jks
   storePassword=SUA_SENHA
   keyAlias=upload
   keyPassword=SUA_SENHA
   ```
3. Gere o pacote: `./gradlew :app:bundleRelease`. O arquivo fica em `app/build/outputs/bundle/release/app-release.aab`.
4. Faça backup do `.jks` e das senhas. Se perder a chave de upload, é possível pedir um reset ao Google, mas dá trabalho.

A cada nova versão, aumente `versionCode` em `app/build.gradle.kts`.

## 2. Teste em aparelho real (antes de enviar)
Instale o build de release (`:app:installRelease`, com a assinatura configurada) ou o debug.
- [ ] Abre sem travar (já houve um crash de release, corrigido).
- [ ] Lista das 10 moedas carrega; o botão de atualizar funciona.
- [ ] Modo avião: aparece a lista salva e o aviso de "dados salvos", sem texto de erro técnico.
- [ ] Fixar uma moeda e receber a notificação (aceite a permissão no Android 13+).
- [ ] Tema claro, escuro e do sistema.
- [ ] Idioma: inglês e português; reinicia a tela sem perder a aba.
- [ ] Configurações: não aparece conta; aparecem os links Legal (Política e Termos) e eles abrem as páginas.
- [ ] Anúncio de banner aparece (no release usa o ID real: **não clique nos seus próprios anúncios**).
- [ ] Tela pequena e fonte grande (escala 1,3x): nada cortado.
- [ ] Android 11: sem blur real, mas legível.

## 3. Play Console

### Política de privacidade
Cole a URL da política acima em *Conteúdo do app → Política de Privacidade*.

### Anúncios
*Conteúdo do app → Anúncios*: **Sim, o app contém anúncios** (AdMob).

### Segurança dos dados
O app não tem conta e não envia dados para servidor próprio. Quem coleta é o SDK do AdMob.
- Coleta ou compartilha dados? **Sim** (por causa do SDK de anúncios).
- Tipos a declarar, conforme o guia oficial do AdMob ("Declarar dados do SDK de anúncios"): **ID do dispositivo ou outros IDs** (ID de publicidade) e os dados de **desempenho e diagnóstico**; confira no guia atual se ele pede também **localização aproximada**.
- Finalidade: **Publicidade ou marketing** e **Análise**.
- Dados criptografados em trânsito: **Sim** (HTTPS).
- Nome, e-mail, localização precisa, contatos, fotos, etc.: **não coletados**.
- Pedido de exclusão de dados: o app não mantém conta; informe isso e aponte o e-mail de contato da política.

Confira o texto do guia do AdMob antes de enviar o formulário, porque o Google atualiza a lista.

### Outras declarações
- Público-alvo: **maiores de 13 anos** (ou 18+); o app não é para crianças.
- Classificação de conteúdo: questionário (app financeiro/informativo, sem violência etc.).
- Categoria: Finanças. O app **não** é corretora nem dá conselho: a descrição deve dizer que é só informativo.
- Países: **Brasil** por enquanto (sem tela de consentimento de anúncios, o UMP, não distribua para o Espaço Econômico Europeu e o Reino Unido).

## 4. Preços: provedores e proteção contra excesso de requisições
- Principal: CoinGecko. Plano B: CoinPaprika (gratuita, sem chave). A CoinCap v2 foi desativada e foi removida.
- Proteção (valores em `AppConfig.RateLimit`): uma requisição por vez; resultado reutilizado por 15 s; provedor que falha (HTTP 429/5xx, timeout, resposta inválida) fica de fora por 30 s, dobrando até 10 min, ou pelo `Retry-After` do servidor. Sem internet não há penalidade.
- Se os dois provedores ficarem indisponíveis, o app mostra os dados salvos.
