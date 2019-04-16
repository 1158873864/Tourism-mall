var api = require('../../../config/api.js');
var util = require('../../../utils/util.js');
var user = require('../../../utils/user.js');

var app = getApp();
Page({
  showUserTagsModal: false,
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
    // 页面渲染完成

  },
  onReady: function() {

  },
  onShow: function() {
    // 页面显示
  },
  onHide: function() {
    // 页面隐藏

  },
  onUnload: function() {
    // 页面关闭

  },

  updatePhone: function (e) {
    //this.data.newMyInfo.email = e.detail.value;
    util.request(api.AuthBindPhone, {
      iv: e.detail.value,
      encryptedData: e.detail.encryptedData
    }, 'POST').then(function (res) {
      if (res.errno === 0) {
        wx.showToast({
          title: '绑定手机号码成功',
          icon: 'success',
          duration: 2000
        });
      }
    });
  },
  wxLogin: function(e) {
    console.log("11")
    user.checkLogin().catch(() => {
      user.loginByWeixin(e.detail.userInfo).then(res => {
        app.globalData.hasLogin = true;
        this.setData({
          showUserTagsModal: true
        })
        
      }).catch((err) => {
        app.globalData.hasLogin = false;
        util.showErrorToast('微信登录失败');
      });

    });
  },

  accountLogin: function () {
    wx.navigateTo({
      url: "/pages/auth/accountLogin/accountLogin"
    });
  },

  userTagsModalCancel: function () {
    this.setData({
      showUserTagsModal: false,
    })
    wx.navigateBack({
      delta: 1
    })
    console.log(this.data.showUserTagsModal)
  },

  userTagsModalConfirm1: function () {
    wx.navigateBack({
      delta: 1
    })
  },

})
