# Aurora Music

Aplicativo Android nativo em Kotlin + Jetpack Compose, com pesquisa de música online, capas, streaming e controles de reprodução.

## Catálogo atual

O app integra a API pública do **Jamendo**, que oferece um catálogo de música independente. Essa integração não dá acesso automático a todo o repertório comercial do Spotify ou YouTube Music.

### Playlist AURA MIX

A tela inicial inclui a playlist **AURA MIX — Batida forte + ritmo que gruda**. Ao tocar nela, o app consulta faixas disponíveis no Jamendo usando buscas de música eletrônica, bass, funk e hip-hop. As faixas retornadas formam uma fila de reprodução; tocar em uma música inicia a fila a partir dela e o botão de próxima música avança para a seguinte. A playlist é atualizada a partir do catálogo online e exige um Client ID válido e conexão com a internet.

## Ativar o catálogo

1. Instale e abra o Aurora Music.
2. Toque em **Obter Client ID gratuito** para abrir o portal de desenvolvedores do Jamendo e registrar uma aplicação.
3. Cole o Client ID fornecido pelo Jamendo no app e toque em **Salvar e carregar catálogo**.
4. Pesquise por nome de música, artista ou termo musical; toque numa faixa para reproduzi-la por streaming.

O Client ID é guardado nas preferências locais do aplicativo. Não coloque tokens privados ou senhas nesse campo.

Confira a licença de cada faixa antes de reutilizar, distribuir ou incluir a música em conteúdo publicado. As permissões variam por faixa. O uso do catálogo e da API segue os [termos do Jamendo](https://devportal.jamendo.com/api_terms_of_use).

## Compilar o APK

O repositório contém um workflow do GitHub Actions. Após um push na branch `main`, ele compila o APK de depuração e publica `app-debug.apk` como artefato da execução.

- Pacote Android: `com.auroramusic.app`
- Versão: `0.3.0`
- Requisitos de build: JDK 17 e Gradle 8.11.1.

O APK de depuração serve para testes; para distribuição pública, configure uma assinatura de release.
