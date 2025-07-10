package panda.vendor.management.services;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class CorrelationServiceUtils {
	
	private final Map<Integer, String> correlationMap = new ConcurrentHashMap<>();
	
	 public String generateForOrder(int orderId) {
	        String correlationId = UUID.randomUUID().toString();
	        correlationMap.put(orderId, correlationId);
	        return correlationId;
	    }

	    public Optional<String> getCorrelationId(int orderId) {
	        return Optional.ofNullable(correlationMap.get(orderId));
	    }

	    public void removeCorrelation(int orderId) {
	        correlationMap.remove(orderId);
	    }
	}

