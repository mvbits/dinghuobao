package com.javamalls.front.web.h5.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.javamalls.base.domain.virtual.SysMap;
import com.javamalls.base.mv.JModelAndView;
import com.javamalls.base.query.support.IPageList;
import com.javamalls.base.tools.CommUtil;
import com.javamalls.platform.domain.Accessory;
import com.javamalls.platform.domain.BargainGoods;
import com.javamalls.platform.domain.Goods;
import com.javamalls.platform.domain.SysConfig;
import com.javamalls.platform.domain.query.BargainGoodsQueryObject;
import com.javamalls.platform.service.IBargainGoodsService;
import com.javamalls.platform.service.ISysConfigService;
import com.javamalls.platform.service.IUserConfigService;

/**折扣商品
 *                       
 * @Filename: BargainViewAction.java
 * @Version: 2.7.0
 * @Author: 范光洲
 * @Email: goodfgz@163.com
 *
 */
@Controller
public class H5BargainViewAction {

    @Autowired
    private ISysConfigService    configService;
    @Autowired
    private IUserConfigService   userConfigService;
    @Autowired
    private IBargainGoodsService bargainGoodsService;

    @RequestMapping({ "/mobile/bargain.htm" })
    public ModelAndView bargain(HttpServletRequest request, HttpServletResponse response,
                                String bg_time, String currentPage, String orderBy, String orderType) {
        ModelAndView mv = new JModelAndView("h5/bargain_list.html",
            this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request,
            response);
        BargainGoodsQueryObject bqo = new BargainGoodsQueryObject(currentPage, mv, orderBy,
            orderType);
        if (CommUtil.null2String(bg_time).equals("")) {
            bqo.addQuery("obj.bg_time",
                new SysMap("bg_time", CommUtil.formatDate(CommUtil.formatShortDate(new Date()))),
                "=");
        } else {
            bqo.addQuery("obj.bg_time", new SysMap("bg_time", CommUtil.formatDate(bg_time)), "=");
        }
        bqo.addQuery("obj.bg_status", new SysMap("bg_status", Integer.valueOf(1)), "=");
        bqo.setPageSize(Constent.BARGAIN_LIST_PAGE_SIZE);
        IPageList pList = this.bargainGoodsService.list(bqo);
        CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);

        Map<String, Object> params = new HashMap<String, Object>();
        Calendar cal = Calendar.getInstance();
        if (CommUtil.null2String(bg_time).equals("")) {
            bg_time = CommUtil.formatShortDate(new Date());
        }
        cal.setTime(CommUtil.formatDate(bg_time));
        cal.add(6, 1);
        params.put("bg_time", CommUtil.formatDate(CommUtil.formatShortDate(cal.getTime())));
        params.put("bg_status", Integer.valueOf(1));
        /*List<BargainGoods> bgs = this.bargainGoodsService
            .query(
                "select obj from BargainGoods obj where obj.bg_time=:bg_time and obj.bg_status=:bg_status order by audit_time desc",
                params, 0, 5);
        mv.addObject("bgs", bgs);*/
        int day_count = this.configService.getSysConfig().getBargain_validity();
        day_count = 3;
        List<Date> dates = new ArrayList<Date>();
        for (int i = 0; i < day_count; i++) {
            cal = Calendar.getInstance();
            cal.add(6, i);
            dates.add(cal.getTime());
        }
        mv.addObject("dates", dates);
        mv.addObject("bg_time", bg_time);
        return mv;
    }

    @RequestMapping({ "/mobile/load_bargain_goods_list.htm" })
    public void load_index_goods_list(HttpServletRequest request, HttpServletResponse response,
                                      String bg_time, String currentPage, String orderBy,
                                      String orderType) {
        SysConfig config = this.configService.getSysConfig();
        ModelAndView mv = new JModelAndView("h5/bargain_list.html", config,
            this.userConfigService.getUserConfig(), 1, request, response);
        BargainGoodsQueryObject bqo = new BargainGoodsQueryObject(currentPage, mv, orderBy,
            orderType);
        if (CommUtil.null2String(bg_time).equals("")) {
            bqo.addQuery("obj.bg_time",
                new SysMap("bg_time", CommUtil.formatDate(CommUtil.formatShortDate(new Date()))),
                "=");
        } else {
            bqo.addQuery("obj.bg_time", new SysMap("bg_time", CommUtil.formatDate(bg_time)), "=");
        }
        bqo.addQuery("obj.bg_status", new SysMap("bg_status", Integer.valueOf(1)), "=");
        bqo.setPageSize(Constent.BARGAIN_LIST_PAGE_SIZE);
        IPageList pList = this.bargainGoodsService.list(bqo);
        List<BargainGoods> mgList = pList.getResult();

        Calendar cal = Calendar.getInstance();
        if (CommUtil.null2String(bg_time).equals("")) {
            bg_time = CommUtil.formatShortDate(new Date());
        }
        cal.setTime(CommUtil.formatDate(bg_time));
        cal.add(6, 1);
        int day_count = this.configService.getSysConfig().getBargain_validity();
        day_count = 3;
        List<Date> dates = new ArrayList<Date>();
        for (int i = 0; i < day_count; i++) {
            cal = Calendar.getInstance();
            cal.add(6, i);
            dates.add(cal.getTime());
        }

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String imgPath = null;
        String goods_url = null;
        Accessory accessory = config.getGoodsImage();
        String webPath = mv.getModelMap().get("webPath").toString();
        String imageWebServer = mv.getModelMap().get("imageWebServer").toString();
        for (BargainGoods mg : mgList) {

            Goods goods = mg.getBg_goods();

            imgPath = imageWebServer + File.separator + accessory.getPath() + File.separator
                      + accessory.getName();
            if (goods.getGoods_main_photo() != null) {
                imgPath = imageWebServer + File.separator + goods.getGoods_main_photo().getPath()
                          + File.separator + goods.getGoods_main_photo().getName() + "_middle."
                          + goods.getGoods_main_photo().getExt();
            }

            goods_url = webPath + "/mobile/goods_" + goods.getId() + ".htm";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", goods.getId());
            map.put("imgPath", imgPath);
            map.put("goods_url", goods_url);
            map.put("bg_price", CommUtil.null2Double(mg.getBg_price()));
            map.put("goods_price", CommUtil.null2Double(goods.getGoods_price()));
            map.put("goods_name", CommUtil.substring(goods.getGoods_name(), 25));
            map.put("currentPage", currentPage);
            if (CommUtil.null2String(bg_time).equals(CommUtil.formatShortDate(dates.get(0)))) {
                if (goods.getGoods_inventory() > 0) {
                    map.put("div", "<div class=\"p_time zzqg\"></div>");
                } else {
                    map.put("div", "<div class=\"p_time yjqw\"></div>");
                }
            } else {
                map.put("div", "<div class=\"p_time jjkq\"></div>");
            }
            list.add(map);
        }
        String temp = Json.toJson(list, JsonFormat.compact());
        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.print(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
