import java.time.Duration;
import org.springframework.web.client.RestClient;

// 在ChatService类中添加初始化方法
@PostConstruct
public void init() {
    this.chatClient = ChatClient.builder(modelFactory.get(modelOptionsBuilder.build()))
        .withRestClient(RestClient.builder()
            .requestFactory(() -> new OkHttp3ClientHttpRequestFactory())
            .connectTimeout(Duration.ofSeconds(30))  // 增加连接超时
            .readTimeout(Duration.ofSeconds(60))     // 增加读取超时
            .build())
        .build();
}

// 修改chat方法中的调用方式
public ChatResult chat(ChatMessage chatMessage) {
    ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatMessage);
    
    // 使用带超时的RestClient执行请求
    ChatResponse response = requestSpec.call().chatResponse();