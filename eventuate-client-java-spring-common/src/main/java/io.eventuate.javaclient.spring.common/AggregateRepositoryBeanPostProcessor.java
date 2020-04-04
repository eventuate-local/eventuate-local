package io.eventuate.javaclient.spring.common;

import io.eventuate.AggregateRepository;
import io.eventuate.CompositeMissingApplyEventMethodStrategy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class AggregateRepositoryBeanPostProcessor implements BeanPostProcessor {

  private final CompositeMissingApplyEventMethodStrategy strategies;

  public AggregateRepositoryBeanPostProcessor(CompositeMissingApplyEventMethodStrategy strategies) {
    this.strategies = strategies;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof AggregateRepository) {
      ((AggregateRepository)bean).setMissingApplyEventMethodStrategy(strategies);
    } else if (bean instanceof io.eventuate.sync.AggregateRepository) {
      ((io.eventuate.sync.AggregateRepository)bean).setMissingApplyEventMethodStrategy(strategies);
    }
    return bean;
  }
}
