package com.training.app.ai.config.openai;

import com.training.app.ai.config.AIConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.ai.openai")
public class OpenAIConfig extends AIConfig {
}
