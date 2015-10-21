package eu.dariah.de.minfba.schereg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.schereg.dao.interfaces.MappingDao;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;

@Service
public class MappingServiceImpl implements MappingService {
	@Autowired private MappingDao mappingDao;
}
