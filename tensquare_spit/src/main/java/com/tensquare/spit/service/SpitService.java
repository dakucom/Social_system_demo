package com.tensquare.spit.service;

import com.tensquare.spit.dao.SpitDao;
import com.tensquare.spit.pojo.Spit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utils.IdWorker;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SpitService {
    @Autowired
    private SpitDao spitDao;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Spit> findAll() {
        return spitDao.findAll();
    }

    public Spit findById(String id) {
        return spitDao.findById(id).orElse(null);
    }

    /*
     * @Description: 添加吐槽
     * @Author: dakuzai
     * @Date: 2020/2/21 16:08
     * @param: Spit
     * @return:
     **/
    public void save(Spit spit) {
        // 指定id
        spit.set_id(String.valueOf(idWorker.nextId()));
        spit.setPublishtime(new Date());        // 发布日期
        spit.setVisits(0);      // 浏览量
        spit.setShare(0);       // 分享数
        spit.setThumbup(0);     // 点赞数
        spit.setComment(0);     // 回复数
        spit.setState("1");     // 状态
        // 如果当前的吐槽有父节点，那么父节点加一
        if (spit.getParentid() != null && !"".equals(spit.getParentid())) {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(spit.getParentid()));
            Update update = new Update();
            update.inc("comment", 1);
            mongoTemplate.updateFirst(query, update, "spit");
        }
        spitDao.save(spit);
    }

    /*
     * @Description:
     * @Author: dakuzai
     * @Date: 2020/2/21 16:46
     * @param Spit:
     * @return: null
     **/
    public void update(Spit spit) {
        spitDao.save(spit);
    }

    public void deleteById(String id) {
        spitDao.deleteById(id);
    }

    /*
     * @Description:点赞
     * @Author: dakuzai
     * @Date: 2020/2/21 16:51
     * @param spitId:
     **/
    public void thumbup(String spitId) {
        // 利用原生mongo命令实现自增  db.spit.update({"_id": "1"}, {$inc: {thumbup: NumberInt(1)}})
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(spitId));
        Update update = new Update();
        update.inc("thumbup", 1);
        mongoTemplate.updateFirst(query, update, "spit");
    }

    public Page<Spit> findByParentId(String parentid, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return spitDao.findByParentid(parentid, pageable);
    }

    public Page<Spit> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return spitDao.findAll(pageable);
    }

    public List<Spit> searchSpit(Spit spit) {
        System.out.println("state======>" + spit.getState());
        System.out.println("content======>" + spit.getContent());
        System.out.println("id======>" + spit.get_id());
        Query query = new Query();
        query.addCriteria(Criteria.where("content").is(spit.getContent()));
        List<Spit> spitList = mongoTemplate.find(query, Spit.class);
        return spitList;
    }
}
