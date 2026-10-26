# Aurora Music 🎧

**Beta 0.1 — projeto Android em desenvolvimento**

Aurora Music é um reprodutor Android com interface moderna, reprodução em segundo plano e controles de mídia do sistema. Esta primeira versão toca arquivos de áudio escolhidos pelo próprio usuário; ainda não inclui catálogo comercial online nem recomendações por IA.

## Recursos iniciais
- Interface em Jetpack Compose
- Reprodução com AndroidX Media3 / ExoPlayer
- Serviço de mídia para reprodução em segundo plano e controles da tela bloqueada
- Seleção de arquivos de áudio no aparelho
- Saída de áudio Bluetooth usando o roteamento normal do Android
- Workflow para compilar APK de debug no GitHub Actions

## Compilar
Abra o projeto no Android Studio com JDK 17+ e deixe o Gradle sincronizar. Também é possível executar o workflow **Android Debug APK** na aba Actions. O APK de debug não é uma versão de produção.

## Catálogo e direitos autorais
A versão inicial não baixa nem inclui músicas protegidas. Um catálogo online futuro deverá usar fontes autorizadas e respeitar licenças, direitos autorais e termos dos provedores.

## Estado
Protótipo inicial. A compilação automatizada precisa ser executada e aprovada antes de considerar o APK validado.
