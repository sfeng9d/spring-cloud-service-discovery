package org.springframework.cloud.client.serviceregistry.endpoint;

import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.Character.isUpperCase;

import com.google.common.collect.ImmutableMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Endpoint to display and set the service instance status using the service registry.
 * see: {@link org.springframework.cloud.client.serviceregistry.endpoint.ServiceRegistryEndpoint}
 *
 * @author haolun zhang
 */
@SuppressWarnings("unchecked")
@Endpoint(id = "service-registry")
@Slf4j
public class MultiServiceRegistryEndpoint {

  private static final Comparator<ServiceRegistry> SERVICE_REGISTRY_COMPARATOR = (o1, o2) -> {
    if (o1 != null && o2 != null) {
      return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
    } else if (o1 != null) {
      return -1;
    } else {
      return 1;
    }
  };

  static String discoveryServiceName(final Class<?> registryOrRegistration) {
    if (registryOrRegistration != null) {
      final String simpleName = registryOrRegistration.getSimpleName();
      final OptionalInt secondUpperCase = IntStream.range(1, simpleName.length())
          .filter(i -> isUpperCase(simpleName.charAt(i)))
          .findFirst();
      return simpleName.substring(0, secondUpperCase.orElse(simpleName.length())).toLowerCase();
    } else {
      return null;
    }
  }

//  private final ServiceRegistry serviceRegistry;

//  private Registration registration;

  private final Map<String, ServiceRegistry> serviceRegistries;

  private List<Registration> registrations;

  private List<Pair<String, Pair<Registration, ServiceRegistry>>> pairs = newLinkedList();

  public MultiServiceRegistryEndpoint(List<ServiceRegistry> serviceRegistries) {
    if (serviceRegistries != null) {
      this.serviceRegistries = serviceRegistries.stream()
          .sorted(SERVICE_REGISTRY_COMPARATOR) //
          .collect(Collectors.toMap(
              key -> discoveryServiceName(key.getClass()), value -> value, // key = name, value = serviceRegistry
              (oldValue, newValue) -> oldValue,                   // if same key, take the old key
              LinkedHashMap::new                                  // returns a LinkedHashMap, keep order
          ));
    } else {
      this.serviceRegistries = ImmutableMap.of();
    }
  }

  public void setRegistrations(List<Registration> registrations) {
    this.registrations = registrations;
    if (registrations != null) {
      registrations.forEach(registration -> {
        final String name = discoveryServiceName(registration.getClass());
        final ServiceRegistry<?> registry = this.serviceRegistries.get(name);
        if (registry != null) {
          this.pairs.add(Pair.of(name, Pair.of(registration, registry)));
        } else {
          log.warn("corresponding service registry for registration {} not found.", registration);
        }
      });
    }
  }

  @WriteOperation
  public ResponseEntity<?> setStatus(String status) {
//    Assert.notNull(status, "status may not by null");
//
//    if (this.registration == null) {
//      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no registration found");
//    }
//
//    this.serviceRegistry.setStatus(this.registration, status);
//    return ResponseEntity.ok().build();

    Assert.notNull(status, "status may not by null");

    if (this.pairs.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no registration found");
    }

    this.pairs.forEach(pair -> {
      final Registration registration = pair.getRight().getLeft();
      final ServiceRegistry serviceRegistry = pair.getRight().getRight();
      serviceRegistry.setStatus(registration, status);
    });
    return ResponseEntity.ok().build();
  }

  @ReadOperation
  public ResponseEntity getStatus() {
//    if (this.registration == null) {
//      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no registration found");
//    }
//
//    return ResponseEntity.ok().body(this.serviceRegistry.getStatus(this.registration));

    if (this.pairs.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no registration found");
    }

    return ResponseEntity.ok().body(
        pairs.stream() //
            .map(pair -> pair.getLeft() + ": " + pair.getRight().getRight().getStatus(pair.getRight().getLeft())) //
            .collect(Collectors.joining(","))
    );
  }
}
