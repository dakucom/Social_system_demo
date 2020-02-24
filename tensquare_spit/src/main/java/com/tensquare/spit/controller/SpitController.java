package com.tensquare.spit.controller;

import com.tensquare.spit.pojo.Spit;
import com.tensquare.spit.service.SpitService;
import entity.PageResult;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/spit")
public class SpitController {
    @Autowired
    private SpitService spitService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("")
    public Result findAll() {
        return new Result(true, StatusCode.OK.getCode(), "查询成功", spitService.findAll());
    }

    @GetMapping("/{spitId}")
    public Result findById(@PathVariable String spitId) {
        return new Result(true, StatusCode.OK.getCode(), "查询成功", spitService.findById(spitId));
    }

    @PostMapping("")
    public Result save(@RequestBody Spit spit) {
        spitService.save(spit);
        return new Result(true, StatusCode.OK.getCode(), "保存成功");
    }

    @PutMapping("/{spitId}")
    public Result update(@RequestBody Spit spit, @PathVariable String spitId) {
        spit.set_id(spitId);
        spitService.update(spit);
        return new Result(true, StatusCode.OK.getCode(), "修改成功");
    }

    @DeleteMapping("/{spitId}")
    public Result delete(@PathVariable String spitId) {
        spitService.deleteById(spitId);
        return new Result(true, StatusCode.OK.getCode(), "删除成功");
    }

    /*
     * @Description:根据上级ID查询吐槽数据（分页）
     * @Author: dakuzai
     * @Date: 2020/2/21 17:11
     * @param parentId:上级id
     * @param page:页码
     * @param size:大小
     * @return: null
     **/
    @GetMapping("/comment/{parentid}/{page}/{size}")
    public Result findByParentId(@PathVariable String parentid, @PathVariable int page, @PathVariable int size) {
        Page<Spit> pageData = spitService.findByParentId(parentid, page, size);
        return new Result(true, StatusCode.OK.getCode(), "查询成功", new PageResult<Spit>(pageData.getTotalElements(), pageData.getContent()));
    }

    /*
     * @Description:spit分页
     * @Author: dakuzai
     * @Date: 2020/2/21 17:40
     * @param page: 页码
     * @param size: 大小
     * @return: Result
     **/
    @PostMapping("/search/{page}/{size}")
    public Result findAll(@PathVariable int page, @PathVariable int size) {
        Page<Spit> pageData = spitService.findAll(page, size);
        return new Result(true, StatusCode.OK.getCode(), "查询成功", new PageResult<Spit>(pageData.getTotalElements(), pageData.getContent()));
    }

    /*
     * @Description:根据条件查询Spit列表
     * @Author: dakuzai
     * @Date: 2020/2/21 17:58
     * @param spit: Spit
     * @return:
     **/
    @PostMapping("/search")
    public Result searchSpit(@RequestBody Spit spit) {
        List<Spit> listSpit = spitService.searchSpit(spit);
        return new Result(true, StatusCode.OK.getCode(), "查询成功",listSpit);
    }

    @PutMapping("/thumbup/{spitId}")
    public Result thumbup(@PathVariable String spitId) {
        // 判断当前用户是否已经点赞，但是现在没有做验证，暂时先把userid写死
        String userid = "111111";
        // 判断当前用户是否已经点赞
        if (redisTemplate.opsForValue().get("thumbup_spit_" + userid) != null) {
            // 已经点赞了
            return new Result(false, StatusCode.REPERROR.getCode(), "不能重复点赞");
        }
        spitService.thumbup(spitId);
        redisTemplate.opsForValue().set("thumbup_spit_" + userid, 1);
        return new Result(true, StatusCode.OK.getCode(), "点赞成功");
    }
}
