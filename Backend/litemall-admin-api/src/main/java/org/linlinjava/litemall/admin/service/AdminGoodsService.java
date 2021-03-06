package org.linlinjava.litemall.admin.service;

import com.github.pagehelper.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.admin.dao.GoodsAllinone;
import org.linlinjava.litemall.admin.dao.ShopGoodsAllinone;
import org.linlinjava.litemall.admin.util.CatVo;
import org.linlinjava.litemall.core.qcode.QCodeService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.linlinjava.litemall.admin.util.AdminResponseCode.GOODS_NAME_EXIST;
import static org.linlinjava.litemall.admin.util.AdminResponseCode.GOODS_UPDATE_NOT_ALLOWED;

@Service
public class AdminGoodsService {
    private final Log logger = LogFactory.getLog(AdminGoodsService.class);


    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallGoodsSpecificationService specificationService;
    @Autowired
    private LitemallGoodsAttributeService attributeService;
    @Autowired
    private LitemallGoodsProductService productService;
    @Autowired
    private LitemallCategoryService categoryService;
    @Autowired
    private LitemallBrandService brandService;
    @Autowired
    private LitemallCartService cartService;
    @Autowired
    private LitemallOrderGoodsService orderGoodsService;

    @Autowired
    private QCodeService qCodeService;



    //被模仿的查询
    @Transactional
    public Object list(String goodsSn, String name,
                       Integer page, Integer limit, String sort, String order) {
        List<LitemallGoods> goodsList = goodsService.querySelective(goodsSn, name, page, limit, sort, order);
        long total = PageInfo.of(goodsList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", goodsList);

        return ResponseUtil.ok(data);
    }

    //商家的查询商品功能
    @Transactional
    public  Object QueryShopOrder(String category_id, String name,
                                  Integer page, Integer limit, String sort, String order){
        System.out.println("category_id"+category_id+"111111111111");
        System.out.println("category_id"+name+"3333333333");
        List<LitemallShopgoods> goodmap=goodsService.querySelectiveBycondition(category_id, name, page, limit, sort,order);
        long total = PageInfo.of(goodmap).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", goodmap);

        return ResponseUtil.ok(data);
    }




    private Object validate(GoodsAllinone goodsAllinone) {
        LitemallGoods goods = goodsAllinone.getGoods();
        String name = goods.getName();
        if (StringUtils.isEmpty(name)) {
            return ResponseUtil.badArgument();
        }
        String goodsSn = goods.getGoodsSn();
        if (StringUtils.isEmpty(goodsSn)) {
            return ResponseUtil.badArgument();
        }
        // 品牌商可以不设置，如果设置则需要验证品牌商存在
        Integer brandId = goods.getBrandId();
        if (brandId != null && brandId != 0) {
            if (brandService.findById(brandId) == null) {
                return ResponseUtil.badArgumentValue();
            }
        }
        // 分类可以不设置，如果设置则需要验证分类存在
        Integer categoryId = goods.getCategoryId();
        if (categoryId != null && categoryId != 0) {
            if (categoryService.findById(categoryId) == null) {
                return ResponseUtil.badArgumentValue();
            }
        }

        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        for (LitemallGoodsAttribute attribute : attributes) {
            String attr = attribute.getAttribute();
            if (StringUtils.isEmpty(attr)) {
                return ResponseUtil.badArgument();
            }
            String value = attribute.getValue();
            if (StringUtils.isEmpty(value)) {
                return ResponseUtil.badArgument();
            }
        }

        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        for (LitemallGoodsSpecification specification : specifications) {
            String spec = specification.getSpecification();
            if (StringUtils.isEmpty(spec)) {
                return ResponseUtil.badArgument();
            }
            String value = specification.getValue();
            if (StringUtils.isEmpty(value)) {
                return ResponseUtil.badArgument();
            }
        }

        LitemallGoodsProduct[] products = goodsAllinone.getProducts();
        for (LitemallGoodsProduct product : products) {
            Integer number = product.getNumber();
            if (number == null || number < 0) {
                return ResponseUtil.badArgument();
            }

            BigDecimal price = product.getPrice();
            if (price == null) {
                return ResponseUtil.badArgument();
            }

            String[] productSpecifications = product.getSpecifications();
            if (productSpecifications.length == 0) {
                return ResponseUtil.badArgument();
            }
        }

        return null;
    }

    /**
     * 编辑商品
     * <p>
     * TODO
     * 目前商品修改的逻辑是
     * 1. 更新litemall_goods表
     * 2. 逻辑删除litemall_goods_specification、litemall_goods_attribute、litemall_goods_product
     * 3. 添加litemall_goods_specification、litemall_goods_attribute、litemall_goods_product
     * <p>
     * 这里商品三个表的数据采用删除再添加的策略是因为
     * 商品编辑页面，支持管理员添加删除商品规格、添加删除商品属性，因此这里仅仅更新是不可能的，
     * 只能删除三个表旧的数据，然后添加新的数据。
     * 但是这里又会引入新的问题，就是存在订单商品货品ID指向了失效的商品货品表。
     * 因此这里会拒绝管理员编辑商品，如果订单或购物车中存在商品。
     * 所以这里可能需要重新设计。
     */
    @Transactional
    public Object update(GoodsAllinone goodsAllinone) {
        Object error = validate(goodsAllinone);
        if (error != null) {
            return error;
        }

        LitemallGoods goods = goodsAllinone.getGoods();
        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        LitemallGoodsProduct[] products = goodsAllinone.getProducts();

        Integer id = goods.getId();
        // 检查是否存在购物车商品或者订单商品
        // 如果存在则拒绝修改商品。
        if (orderGoodsService.checkExist(id)) {
            return ResponseUtil.fail(GOODS_UPDATE_NOT_ALLOWED, "商品已经在订单中，不能修改");
        }
        if (cartService.checkExist(id)) {
            return ResponseUtil.fail(GOODS_UPDATE_NOT_ALLOWED, "商品已经在购物车中，不能修改");
        }

        //将生成的分享图片地址写入数据库
        String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());
        goods.setShareUrl(url);

        // 商品基本信息表litemall_goods
        if (goodsService.updateById(goods) == 0) {
            throw new RuntimeException("更新数据失败");
        }

        Integer gid = goods.getId();
        specificationService.deleteByGid(gid);
        attributeService.deleteByGid(gid);
        productService.deleteByGid(gid);

        // 商品规格表litemall_goods_specification
        for (LitemallGoodsSpecification specification : specifications) {
            specification.setGoodsId(goods.getId());
            specificationService.add(specification);
        }

        // 商品参数表litemall_goods_attribute
        for (LitemallGoodsAttribute attribute : attributes) {
            attribute.setGoodsId(goods.getId());
            attributeService.add(attribute);
        }

        // 商品货品表litemall_product
        for (LitemallGoodsProduct product : products) {
            product.setGoodsId(goods.getId());
            productService.add(product);
        }
        qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());

        return ResponseUtil.ok();
    }

    @Transactional
    public Object delete(LitemallGoods goods) {
        Integer id = goods.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }

        Integer gid = goods.getId();
        goodsService.deleteById(gid);
        specificationService.deleteByGid(gid);
        attributeService.deleteByGid(gid);
        productService.deleteByGid(gid);
        return ResponseUtil.ok();
    }

    //商家删除的商品
    @Transactional
    public Object deleteGoods(LitemallShopgoods goods) {
        Integer id = goods.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }

        Integer gid = goods.getId();
        goodsService.deleteGoodsById(gid);
        specificationService.deleteByGid(gid);
        attributeService.deleteByGid(gid);
        productService.deleteByGid(gid);
        return ResponseUtil.ok();
    }



    @Transactional
    public Object create(GoodsAllinone goodsAllinone) {
        Object error = validate(goodsAllinone);
        if (error != null) {
            return error;
        }

        LitemallGoods goods = goodsAllinone.getGoods();
        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        LitemallGoodsProduct[] products = goodsAllinone.getProducts();

        String name = goods.getName();
        if (goodsService.checkExistByName(name)) {
            return ResponseUtil.fail(GOODS_NAME_EXIST, "商品名已经存在");
        }

        // 商品基本信息表litemall_goods
        goodsService.add(goods);

        //将生成的分享图片地址写入数据库
        String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());
        if (!StringUtils.isEmpty(url)) {
            goods.setShareUrl(url);
            if (goodsService.updateById(goods) == 0) {
                throw new RuntimeException("更新数据失败");
            }
        }

        // 商品规格表litemall_goods_specification
        for (LitemallGoodsSpecification specification : specifications) {
            specification.setGoodsId(goods.getId());
            specificationService.add(specification);
        }

        // 商品参数表litemall_goods_attribute
        for (LitemallGoodsAttribute attribute : attributes) {
            attribute.setGoodsId(goods.getId());
            attributeService.add(attribute);
        }

        // 商品货品表litemall_product
        for (LitemallGoodsProduct product : products) {
            product.setGoodsId(goods.getId());
            productService.add(product);
        }
        return ResponseUtil.ok();
    }

    //商家的添加商品
    @Transactional
    public Object createShopGoods(ShopGoodsAllinone goodsAllinone) {
        System.out.println("11111111111=======0000000000");
        Object error = validateByGoods(goodsAllinone);
        System.out.println("goodsAllinone+"+goodsAllinone);
        if (error != null) {
            return error;
        }

        //LitemallGoods goods = goodsAllinone.getGoods();
        LitemallShopgoods goods=goodsAllinone.getgoodstwo();
        System.out.println("service======"+goods.getName()+"0000000000");
        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        LitemallGoodsProduct[] products = goodsAllinone.getProducts();

        String name = goods.getName();
        if (goodsService.checkExistByGoodsName(name)) {
            return ResponseUtil.fail(GOODS_NAME_EXIST, "商品名已经存在");
        }

        // 商品基本信息表litemall_goods
        goods.setId(null);
        goodsService.addGoods(goods);

        //将生成的分享图片地址写入数据库
        String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());
        if (!StringUtils.isEmpty(url)) {
            goods.setShareUrl(url);
            if (goodsService.updateByGoodsId(goods) == 0) {
                throw new RuntimeException("更新数据失败");
            }
        }

        // 商品规格表litemall_goods_specification
        for (LitemallGoodsSpecification specification : specifications) {
            specification.setGoodsId(goods.getId());
            specificationService.add(specification);
        }

        // 商品参数表litemall_goods_attribute
        for (LitemallGoodsAttribute attribute : attributes) {
            attribute.setGoodsId(goods.getId());
            attributeService.add(attribute);
        }

        // 商品货品表litemall_product
        for (LitemallGoodsProduct product : products) {
            product.setGoodsId(goods.getId());
            productService.add(product);
        }
        return ResponseUtil.ok();
    }


    public Object list2() {
        // http://element-cn.eleme.io/#/zh-CN/component/cascader
        //        // 管理员设置“所属分类”
        List<LitemallCategory> l1CatList = categoryService.queryL1();
        List<CatVo> categoryList = new ArrayList<>(l1CatList.size());

        for (LitemallCategory l1 : l1CatList) {
            CatVo l1CatVo = new CatVo();
            l1CatVo.setValue(l1.getId());
            l1CatVo.setLabel(l1.getName());

            List<LitemallCategory> l2CatList = categoryService.queryByPid(l1.getId());
            List<CatVo> children = new ArrayList<>(l2CatList.size());
            for (LitemallCategory l2 : l2CatList) {
                CatVo l2CatVo = new CatVo();
                l2CatVo.setValue(l2.getId());
                l2CatVo.setLabel(l2.getName());
                children.add(l2CatVo);
            }
            l1CatVo.setChildren(children);

            categoryList.add(l1CatVo);
        }

        // http://element-cn.eleme.io/#/zh-CN/component/select
        // 管理员设置“所属品牌商”
        List<LitemallBrand> list = brandService.all();
        List<Map<String, Object>> brandList = new ArrayList<>(l1CatList.size());
        for (LitemallBrand brand : list) {
            Map<String, Object> b = new HashMap<>(2);
            b.put("value", brand.getId());
            b.put("label", brand.getName());
            brandList.add(b);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("categoryList", categoryList);
        data.put("brandList", brandList);
        return ResponseUtil.ok(data);
    }

    //详情
    @Transactional
    public Object detail(Integer id) {
        System.out.println("detail==="+id+"====detail");
        LitemallGoods goods = goodsService.findById(id);
        System.out.println("detail==="+goods.getName()+"====detail");
        List<LitemallGoodsProduct> products = productService.queryByGid(id);
        System.out.println("products.size==="+products.size()+"====products.size");
        List<LitemallGoodsSpecification> specifications = specificationService.queryByGid(id);
        System.out.println("products.size==="+specifications.size()+"====products.size");
        List<LitemallGoodsAttribute> attributes = attributeService.queryByGid(id);
        System.out.println("products.size==="+attributes.size()+"====products.size");

        Integer categoryId = goods.getCategoryId();
        System.out.println("categoryId==="+categoryId+"====categoryId");
        LitemallCategory category = categoryService.findcategoryById(categoryId);
        System.out.println("category==="+category.getName()+"====category");
        Integer[] categoryIds = new Integer[]{};
        if (category != null) {
            Integer parentCategoryId = category.getPid();
            categoryIds = new Integer[]{parentCategoryId, categoryId};
        }

        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        data.put("specifications", specifications);
        data.put("products", products);
        data.put("attributes", attributes);
        data.put("categoryIds", categoryIds);
        System.out.println("data.size()==="+data.size()+"====data.size()");
        return ResponseUtil.ok(data);
    }

    //商家点击查看详情
    @Transactional
    public Object detailByGoods(Integer id) {
        //查看商品的具体信息
        LitemallShopgoods goods = goodsService.findGoodsById(id);
        //查看商品的物品信息
        List<LitemallGoodsProduct> products = productService.queryByGid(id);
        //查看商品的规格信息
        List<LitemallGoodsSpecification> specifications = specificationService.queryByGid(id);
        //查看商品的属性信息
        List<LitemallGoodsAttribute> attributes = attributeService.queryByGid(id);

        Integer categoryId = goods.getCategoryId();
        //查看商品的类别信息
        LitemallCategory category = categoryService.findById(categoryId);
        Integer[] categoryIds = new Integer[]{};
        if (category != null) {
            Integer parentCategoryId = category.getPid();
            categoryIds = new Integer[]{parentCategoryId, categoryId};
        }

        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        data.put("specifications", specifications);
        data.put("products", products);
        data.put("attributes", attributes);
        data.put("categoryIds", categoryIds);

        return ResponseUtil.ok(data);
    }


    //*****************商家的修改***************************************

    private Object validateByGoods(ShopGoodsAllinone goodsAllinone) {
        LitemallShopgoods goods = goodsAllinone.getgoodstwo();

        String name = goods.getName();
        if (StringUtils.isEmpty(name)) {
            return ResponseUtil.badArgument();
        }
        String goodsSn = goods.getGoodsSn();
        if (StringUtils.isEmpty(goodsSn)) {
            return ResponseUtil.badArgument();
        }
        // 品牌商可以不设置，如果设置则需要验证品牌商存在
        Integer brandId = goods.getBrandId();
        if (brandId != null && brandId != 0) {
            if (brandService.findById(brandId) == null) {
                return ResponseUtil.badArgumentValue();
            }
        }
        // 分类可以不设置，如果设置则需要验证分类存在
        Integer categoryId = goods.getCategoryId();
        if (categoryId != null && categoryId != 0) {
            if (categoryService.findcategoryById(categoryId) == null) {
                return ResponseUtil.badArgumentValue();
            }
        }

        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        for (LitemallGoodsAttribute attribute : attributes) {
            String attr = attribute.getAttribute();
            if (StringUtils.isEmpty(attr)) {
                return ResponseUtil.badArgument();
            }
            String value = attribute.getValue();
            if (StringUtils.isEmpty(value)) {
                return ResponseUtil.badArgument();
            }
        }

        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        for (LitemallGoodsSpecification specification : specifications) {
            String spec = specification.getSpecification();
            if (StringUtils.isEmpty(spec)) {
                return ResponseUtil.badArgument();
            }
            String value = specification.getValue();
            if (StringUtils.isEmpty(value)) {
                return ResponseUtil.badArgument();
            }
        }

        LitemallGoodsProduct[] products = goodsAllinone.getProducts();
        for (LitemallGoodsProduct product : products) {
            Integer number = product.getNumber();
            if (number == null || number < 0) {
                return ResponseUtil.badArgument();
            }

            BigDecimal price = product.getPrice();
            if (price == null) {
                return ResponseUtil.badArgument();
            }

            String[] productSpecifications = product.getSpecifications();
            if (productSpecifications.length == 0) {
                return ResponseUtil.badArgument();
            }
        }

        return null;
    }


    /**
     * 编辑商品
     */
   //商家的修改功能
    @Transactional
    public Object updateShopGoods(ShopGoodsAllinone goodsAllinone) {


        LitemallShopgoods goods =goodsAllinone.getgoodstwo();
        System.out.println(goods.getUpdateTime());

        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        LitemallGoodsProduct[] products = goodsAllinone.getProducts();

        Integer id = goods.getId();
        // 检查是否存在购物车商品或者订单商品
        // 如果存在则拒绝修改商品。

        //个人理解：下面这个判断模块要不要问题不大
        if (orderGoodsService.checkExistByGoodsId(id)) {
            return ResponseUtil.fail(GOODS_UPDATE_NOT_ALLOWED, "商品已经在订单中，不能修改");
        }
        if (cartService.checkExist(id)) {
            return ResponseUtil.fail(GOODS_UPDATE_NOT_ALLOWED, "商品已经在购物车中，不能修改");
        }
        //将生成的分享图片地址写入数据库
        String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());
        goods.setShareUrl(url);

        // 商品基本信息表litemall_goods
        if (goodsService.updateByGoodsId(goods) == 0) {
            throw new RuntimeException("更新数据失败");
        }

        Integer gid = goods.getId();
        specificationService.deleteByGid(gid);
        attributeService.deleteByGid(gid);
        productService.deleteByGid(gid);

        // 商品规格表litemall_goods_specification
        for (LitemallGoodsSpecification specification : specifications) {
            specification.setGoodsId(goods.getId());
            specificationService.add(specification);
        }

        // 商品参数表litemall_goods_attribute
        for (LitemallGoodsAttribute attribute : attributes) {
            attribute.setGoodsId(goods.getId());
            attributeService.add(attribute);
        }

        // 商品货品表litemall_product
        for (LitemallGoodsProduct product : products) {
            product.setGoodsId(goods.getId());
            productService.add(product);
        }
        qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());

        return ResponseUtil.ok();
    }

}
