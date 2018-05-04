package de.unibamberg.minf.dme.dao;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import de.unibamberg.minf.core.util.InitializingObjectMapper;
import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.PersistedSessionDao;
import de.unibamberg.minf.dme.model.LogEntry;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.processing.model.SerializableResource;
import de.unibamberg.minf.processing.model.base.Resource;

@Repository
public class PersistedSessionDaoImpl extends BaseDaoImpl<PersistedSession> implements PersistedSessionDao {
	@Autowired private InitializingObjectMapper objectMapper;
	
	private static final String SAMPLE_OUTPUT = "/output.json";;
	private static final String SAMPLE_MAPPED = "/mapped.json";
	private static final String SAMPLE_INPUT = "/sample.json";
	private static final String SAMPLE_SELECTED_VALUE_MAP = "/valueMap.json"; 
	private static final String SAMPLE_SESSION_LOG = "/sessionLog.json";
	
	/**
	 * Helper class for JSON serializing resources
	 */
	private class ResourceList extends ArrayList<Resource> {
		private static final long serialVersionUID = 4998781729252376207L;
		public ResourceList(List<Resource> sampleOutput) {
			super(sampleOutput);
		}
	}
	
	@Value(value="${paths.sessionData}")
	private String sessionsPath;
	
	public PersistedSessionDaoImpl() {
		super(PersistedSession.class);
	}
	
	@Override
	public PersistedSession findById(String id) {
		return this.loadData(super.findById(id));
	}

	@Override
	public PersistedSession findOne(Query q) {
		return this.loadData(super.findOne(q));
	}

	@Override
	public PersistedSession findOne(Query q, Sort sort) {
		return this.loadData(super.findOne(q, sort));
	}
	
	@Override
	public <S extends PersistedSession> S save(S entity) {
		if (entity.getId()==null) {
			entity.setId(new ObjectId().toString());
		}
		this.saveData(entity);
		
		PersistedSession saveSession = new PersistedSession();
		saveSession.setCreated(entity.getCreated());
		saveSession.setEntityId(entity.getEntityId());
		saveSession.setHttpSessionId(entity.getHttpSessionId());
		saveSession.setId(entity.getId());
		saveSession.setLabel(entity.getLabel());
		saveSession.setLastAccessed(entity.getLastAccessed());
		saveSession.setNotExpiring(entity.isNotExpiring());
		saveSession.setSelectedOutputIndex(entity.getSelectedOutputIndex());
		saveSession.setUserId(entity.getUserId());
		saveSession.setSampleFile(entity.getSampleFile());
		saveSession.setSampleInputType(entity.getSampleInputType());
		
		super.save(saveSession);
		
		entity.setId(saveSession.getId());
		return entity;
	}

	@Override
	public void delete(String id) {
		this.deleteData(id);
		super.delete(id);
	}

	@Override
	public void delete(PersistedSession entity) {
		if (entity.getId()!=null) {
			this.deleteData(entity.getId());
		}
		super.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends PersistedSession> entities) {
		for (PersistedSession entity : entities) {
			if (entity.getId()!=null) {
				this.deleteData(entity.getId());
			}
		}
		super.delete(entities);
	}

	@Override
	public int delete(Collection<String> ids) {
		for (String id : ids) {
			this.deleteData(id);
		}
		return super.delete(ids);
	}
	
	private PersistedSession loadData(PersistedSession session) {
		if (session==null) {
			return null;
		}
		try {
			Path sessionDirectoryPath = Paths.get(sessionsPath + File.separator + session.getId());
			if (!Files.exists(sessionDirectoryPath)) {
				return session;
			}
			
			TypeReference<HashMap<String, String>> mapRef = new TypeReference<HashMap<String, String>>() {};
			TypeReference<ArrayList<SerializableResource>> resourceListRef = new TypeReference<ArrayList<SerializableResource>>() {};
			TypeReference<ArrayList<LogEntry>> logListRef = new TypeReference<ArrayList<LogEntry>>() {};
			
			try {
				File sampleOutput = new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_OUTPUT);
				if (sampleOutput.exists()) {
					session.setSampleOutput(objectMapper.readValue(sampleOutput, resourceListRef));
				}
				File sampleInput = new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_INPUT);
				if (sampleInput.exists()) {
					Path path = Paths.get(sessionDirectoryPath.toAbsolutePath() + SAMPLE_INPUT);
					String read = new String (Files.readAllBytes(path));
					session.setSampleInput(read);
				}
				File sampleMapped = new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_MAPPED);
				if (sampleMapped.exists()) {
					session.setSampleMapped(objectMapper.readValue(sampleMapped, resourceListRef));
				}
				File sampleValueMap = new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_SELECTED_VALUE_MAP);
				if (sampleValueMap.exists()) {
					session.setSelectedValueMap(objectMapper.readValue(sampleValueMap, mapRef));
				}	
			} catch (Exception e) {
				logger.error("Failed to deserialize session data", e);
			}
			
			File sampleSessionLog = new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_SESSION_LOG);
			if (sampleSessionLog.exists()) {
				session.setSessionLog(objectMapper.readValue(sampleSessionLog, logListRef));
			}

		} catch (Exception e) {
			logger.error("Failed to write session data to filesystem", e);
		}
		return session;
	}
	
	private void saveData(PersistedSession session) {
		try {
			Path sessionDirectoryPath = Paths.get(sessionsPath + File.separator + session.getId());
			if (Files.exists(sessionDirectoryPath) && !Files.isDirectory(sessionDirectoryPath)) {
				Files.delete(sessionDirectoryPath);
			}
			if (!Files.exists(sessionDirectoryPath)) {
				Files.createDirectories(sessionDirectoryPath);
			}
			
			if (session.getSampleOutput()!=null && session.getSampleOutput().size()>0) {
				objectMapper.writeValue(new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_OUTPUT), new ResourceList(session.getSampleOutput()));
			}
			if (session.getSampleMapped()!=null && session.getSampleMapped().size()>0) {	
				objectMapper.writeValue(new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_MAPPED), new ResourceList(session.getSampleMapped()));
			}
			if (session.getSampleInput()!=null && session.getSampleInput().length()>0) {
				Path path = Paths.get(sessionDirectoryPath.toAbsolutePath() + SAMPLE_INPUT);
			    byte[] strToBytes = session.getSampleInput().getBytes();
			    Files.write(path, strToBytes);
			}
			if (session.getSelectedValueMap()!=null && session.getSelectedValueMap().size()>0) {
				objectMapper.writeValue(new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_SELECTED_VALUE_MAP), session.getSelectedValueMap());
			}
			if (session.getSessionLog()!=null && session.getSessionLog().size()>0) {
				objectMapper.writeValue(new File(sessionDirectoryPath.toAbsolutePath() + SAMPLE_SESSION_LOG), session.getSessionLog());
			}
		} catch (Exception e) {
			logger.error("Failed to write session data to filesystem", e);
		}
	}
	
	private void deleteData(String sessionId) {
		try {
			Path sessionDirectoryPath = Paths.get(sessionsPath + File.separator + sessionId);
			if (Files.exists(sessionDirectoryPath)) {
				Files.delete(sessionDirectoryPath);
			}
		} catch (Exception e) {
			logger.error("Failed to delete session data", e);
		}
	}
}
