# Serviço de Upload, Conversão e Streaming de Vídeo

Este projeto é um serviço back-end (Spring Boot) para receber uploads de vídeo, converter em múltiplas resoluções usando ffmpeg, armazenar os arquivos (original e convertidos) no MinIO e fornecer streaming HTTP com suporte a Range (permite reprodução em players e seek).

![Screenshot 2026-03-15 220045.png](imgs/Screenshot%202026-03-15%20220045.png)

## Resumo rápido
- Upload: POST /stream/upload (multipart/form-data, campo `file`)
- Conversão: o serviço gera múltiplas resoluções (144, 240, 360, 480, 720, 1080) usando ffmpeg
- Armazenamento: MinIO (S3-compatible). O original e os convertidos são enviados ao bucket configurado
- Streaming: GET /stream/{objectName} com suporte ao header `Range` para streaming parcial

## Visão geral do código
- `com.cesarzxk.initial.demo.controller.StreamController` — endpoints HTTP
- `com.cesarzxk.initial.demo.services.StreamService` — lógica de conversão e preparo de DTOs
- `com.cesarzxk.initial.demo.services.VideoConversionService` — wrapper que chama `ffmpeg` para gerar as resoluções
- `com.cesarzxk.initial.demo.services.StorageService` — integração com MinIO (upload, download, stat)
- `com.cesarzxk.initial.demo.dto.UploadResponseDTO` — DTO de resposta do upload

## Requisitos
- Java 17+ (ou versão compatível com o projeto)
- Maven (ou use o wrapper `mvnw` / `mvnw.cmd` que já está no projeto)
- ffmpeg instalado e disponível no PATH (usado para conversão)
- MinIO (ou qualquer S3 compatível) acessível a partir da aplicação

## Configuração
Edite `src/main/resources/application.properties` ou defina variáveis de ambiente para configurar o MinIO (os exemplos abaixo usam `application.properties`):

```
minio.endpoint=http://SEU_MINIO_HOST:9000
minio.accessKey=seuAccessKey
minio.secretKey=suaSecretKey
minio.bucket=nome-do-bucket
server.port=8080
```

**Observações importantes:**
- A porta do endpoint deve ser a porta da API S3 do MinIO (normalmente `9000`). NÃO a porta do console (ex.: `9021`). Se apontar para a porta do console, você pode ver o erro: "S3 API Requests must be made to API port.".
- As credenciais devem ter permissão para criar/ler/colocar objetos no bucket.

## Como construir e executar (Windows PowerShell)

1. Build:

```powershell
.\mvnw.cmd -DskipTests clean package
```

2. Executar:

```powershell
# usando o JAR gerado
java -jar target\demo-0.0.1-SNAPSHOT.jar

# ou executar via mvn (desenvolvimento)
.\mvnw.cmd spring-boot:run
```

## Endpoints principais

1) POST /stream/upload
- Descrição: recebe um arquivo de vídeo, armazena o original no storage, converte para múltiplas resoluções, faz upload dos convertidos para o storage e retorna os ids/nome dos objetos.
- Body: multipart/form-data com campo `file` contendo o vídeo
- Resposta (exemplo):

```json
{
  "message": "Vídeo convertido com sucesso",
  "files": { "144": "<objectName-144>", "240": "..." },
  "urls": { "144": "<objectName-144>", "240": "..." },
  "storageId": "<objectName-original>"
}
```

2) GET /stream/{objectName}
- Descrição: faz streaming do objeto armazenado no MinIO (é preciso usar o `objectName` retornado no upload). Suporta header `Range` para reprodução parcial/seek.
- Headers relevantes: `Range: bytes=<start>-<end>`
- Exemplo sem Range (baixa todo o arquivo):

```bash
curl -v http://localhost:8080/stream/<objectName> -o out.mp4
```

- Exemplo com Range (pede os primeiros 100KB):

```bash
curl -v -H "Range: bytes=0-102400" http://localhost:8080/stream/<objectName> -o part.mp4
```

## Como testar com PowerShell

Upload:
```powershell
$resp = Invoke-RestMethod -Uri "http://localhost:8080/stream/upload" -Method Post -Form @{ file = Get-Item 'C:\caminho\para\video.mp4' }
Write-Output ($resp | ConvertTo-Json -Depth 5)
```

Streaming (pegar um range):
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/stream/<objectName>" -Method Get -Headers @{ Range = 'bytes=0-102400' } -OutFile 'C:\temp\part.mp4'
```

## Problemas comuns e soluções
- Erro: "S3 API Requests must be made to API port." — a causa mais comum é apontar `minio.endpoint` para a porta do console MinIO em vez da porta S3 API. Ajuste `minio.endpoint` para usar a porta 9000 (ex.: `http://192.168.31.2:9000`).
- Erro: `minioClient is null` — certifique-se de que o bean `MinioClient` está configurado corretamente e que a aplicação carrega as propriedades. Veja os logs ao iniciar.
- ffmpeg não encontrado — instale o ffmpeg e verifique `ffmpeg -version` no PATH.
- Permissões de bucket — verifique se o usuário configurado tem permissões no bucket e se o bucket existe ou será criado pela aplicação.

## Estrutura de diretórios de tempo de execução (local)
- `videos/temp` — arquivos temporários originais durante processamento
- `videos/encoded` — arquivos gerados localmente (depois enviados ao storage, são apagados localmente)

## Observações de segurança e produção
- Em produção, não coloque credenciais sensíveis direto no arquivo `application.properties` no repositório. Use variáveis de ambiente ou um cofre de segredos.
- Considere gerar presigned URLs para streaming direto (mais rápido) e/ou integrar CDN para entrega em larga escala.

## Melhorias possíveis (próximos passos)
- Retornar presigned URLs no campo `urls` em vez de apenas objectNames
- Adicionar um `@ControllerAdvice` para padronizar respostas de erro
- Adicionar métricas e logs detalhados do processo de conversão
- Adicionar testes automatizados para `StreamService` e `StorageService`

## Contribuição
- Sinta-se à vontade para abrir issues ou pull requests. Use o padrão de commits do projeto e mantenha o estilo do código.

## Licença
- (adicionar aqui a licença do projeto se houver)

---
Se quiser, eu posso também gerar presigned URLs automaticamente para os arquivos convertidos e atualizar a resposta `urls` para conter links diretos (diga qual validade prefere, por exemplo: 1h). Também posso mover a etapa de upload do arquivo original para dentro do `StreamService` caso queira centralizar a lógica no serviço (em vez do controller). | Fim do README
