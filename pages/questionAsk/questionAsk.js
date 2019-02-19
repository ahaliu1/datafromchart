var app = getApp();
Page({

  /**
   * 页面的初始数据
   */
  data: {
    images: null,
  },



  chooseImage(e) {
    var that = this;
    wx.chooseImage({
      sizeType: ['compressed'],//'original',
      sourceType: ['album', 'camera'],
      success: res => {
        const images =res.tempFilePaths
        // this.data.images = images.length <= 3 ? images : images.slice(0, 3)
        that.setData({
          images: images
        })
      }
    })
  },

  removeImage(e) {
    //const idx = e.target.dataset.idx
    //this.data.images.splice(idx, 1)
    this.setData({
      images: null
    })
  },

  handleImagePreview(e) {
    //const idx = e.target.dataset.idx
    const images = this.data.images

    wx.previewImage({
      current: images,
      urls: images,
    })
  },

  submitForm(e) {
    var that = this
    wx.showLoading({
      title: '正在创建...',
      mask: true
    })

    //for (let path of that.data.images) {
      wx.uploadFile({
        url: app.globalData.ip + '/upload',
        filePath: that.data.images[0],
        name: 'file',
        formData: {
          'questionOpenId': null
        },
        success: function (res) {
          if (res.data!=""){
            that.downloadFile(res.data)
            wx.hideLoading()
          }
        }
      })
  },


  /**
  
  * 下载文件并预览
  
  */

  downloadFile: function (e) {
    let url = app.globalData.ip +"/"+e//e.currentTarget.dataset.url;
    wx.downloadFile({
      url: url,
      header: {},
      success: function (res) {
        var filePath = res.tempFilePath;
        console.log(filePath);
        wx.openDocument({
          filePath: filePath,
          success: function (res) {
            console.log('打开文档成功')
          },
         fail: function (res) {
            console.log(res);
          },
          complete: function (res) {
           console.log(res);
          }
        })
      },
      fail: function (res) {
        console.log('文件下载失败');
      },
      complete: function (res) { },
    })
  }
})