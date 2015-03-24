package eu.dariah.de.minfba.schereg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.schereg.dao.ElementDao;

@Service
public class ElementServiceImpl implements ElementService {
	@Autowired private ElementDao elementDao;
}
