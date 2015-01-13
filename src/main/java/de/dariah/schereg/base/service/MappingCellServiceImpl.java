package de.dariah.schereg.base.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.schereg.base.dao.MappingCellDao;
import de.dariah.schereg.base.dao.MappingCellInputDao;
import de.dariah.schereg.base.dao.MappingDao;
import de.dariah.schereg.base.model.MappingCell;
import de.dariah.schereg.base.model.MappingCellInput;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class MappingCellServiceImpl implements MappingCellService {

	@Autowired
	private MappingCellDao mappingCellDao;
	
	@Autowired
	private MappingCellInputDao mappingCellInputDao;
	
	@Autowired
	private MappingDao mappingDao;
	
	@Override
	public MappingCell getMappingCell(int id) {
		return mappingCellDao.findById(id);
	}

	@Override
	public List<MappingCell> getMappingCellsForOutput(int mappingId, int output) {
		List<Criterion> crList = new ArrayList<Criterion>();
		crList.add(Restrictions.eq("mapping.id", mappingId));
		crList.add(Restrictions.eq("output", output));
		
		return mappingCellDao.findByCriteria(crList);
	}
	
	@Override
	public List<MappingCell> getMappingCellsForInputOutput(int mappingId, int input, int output) {
		return mappingCellDao.findAllByInputOutput(mappingId, input, output);
	}

	@Override
	public MappingCell createOrLoadCell(int mappingId, int id) {
		MappingCell mc;
		if (id > 0) {
			mc = mappingCellDao.findById(id);
		} else {
			mc = new MappingCell();
			mc.setMapping(mappingDao.findById(mappingId));
			mc.setCreated(DateTime.now());
		}
				
		return mc;
	}

	@Override
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=false)
	public void mergeAndSave(MappingCell mc, Set<Integer> mciElementIds) {
		
		Set<Integer> preMciElementIds = new HashSet<Integer>();
		Set<MappingCellInput> preMcis = new HashSet<MappingCellInput>();
		
		if (mc.getMappingCellInputs()!=null) {
			for (MappingCellInput existing : mc.getMappingCellInputs()) {
				preMcis.add(existing);
				preMciElementIds.add(existing.getElementID());
			}
		}

		for (MappingCellInput pre : preMcis) {
			if (!mciElementIds.contains(pre.getElementID())) {
				mc.getMappingCellInputs().remove(pre);
			}
		}
		
		for (Integer mciElementId : mciElementIds) {
			if (!preMciElementIds.contains(mciElementId)) {
				MappingCellInput mci = new MappingCellInput();
				mci.setMappingCell(mc);
				mci.setElementID(mciElementId);
				
				mc.addMappingCellInput(mci);
			}
		}
		
		
		
		mappingCellDao.saveOrUpdate(mc);
	}

	@Override
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=false)
	public void remove(MappingCell mc) {
		mappingCellDao.delete(mc);
	}

	@Override
	public List<MappingCell> getMappingCellsForInput(int mappingId, int inputCellId) {
		return mappingCellDao.findByMappingAndInputCell(mappingId, inputCellId);
	}
	
	/*@Override
	public List<MappingCell> getMappingCellsForInput(int mappingId, int inputCellId) {

		List<Criterion> crList = new ArrayList<Criterion>();
		crList.add(Restrictions.eq("mappingCell.mapping.id", mappingId));
		crList.add(Restrictions.eq("id", inputCellId));
		
		List<MappingCellInput> inputs = mappingCellInputDao.findByCriteria(crList);
		
		if (inputs==null) {
			return null;
		}
		
		List<MappingCell> mappingCells = new ArrayList<MappingCell>();
		
		for (MappingCellInput input : inputs) {
			if (!mappingCells.contains(input.getMappingCell())) {
				mappingCells.add(input.getMappingCell());
			}
		}
		
		return mappingCells;
	}*/
}
