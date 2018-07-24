package org.springframework.boot.actuate.endpoint;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ShowAllEndpoint extends AbstractEndpoint<List<Endpoint>> {

  private List<Endpoint> endpoints;

  public ShowAllEndpoint(final List<Endpoint> endpoints) {
    super("show-all");
    this.endpoints = endpoints;
  }

  @Override
  public List<Endpoint> invoke() {
    return this.endpoints;
  }

  @Autowired(required = false)
  public void setObjectMappers(final List<com.fasterxml.jackson.databind.ObjectMapper> objectMappers) {
    objectMappers.forEach(objectMapper ->
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    );
  }
}
