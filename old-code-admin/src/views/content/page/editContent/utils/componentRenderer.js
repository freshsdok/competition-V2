import { h } from 'vue';
import { cloneDeep } from 'lodash';
import './backgroundStyles.css';
/**
 * 获取组件渲染器 - 修改为更可靠的实现
 * @param {string} type - 组件类型
 * @returns {Object} 组件渲染器配置对象
 */
export const getComponentRenderer = (type) => {
  // 返回一个普通对象而不是functional组件配置对象
  // 这样可以避免一些Vue渲染的潜在问题
  return {
    props: ['componentData'],
    render() {
      const { componentData } = this
      // 确保componentData存在
      if (!componentData) {
        return h('div', '组件数据加载中...')
      }
      
      // 确保类型存在
      const componentType = componentData.type || type || 'unknown'
      
      // 构建基本样式
      // 1. 先设置默认样式
      // 2. 然后应用componentData.style中的样式
      // 注意：不再使用componentData直接存储的样式属性，确保样式一致性
      const baseStyle = {
        // 默认基础样式
        padding: '10px',
        borderRadius: '0px',
        border: '1px solid #e0e0e0',
        // 默认文本样式
        fontSize: '14px',
        color: '#303133',
        textAlign: 'left',
        // 应用componentData.style中的样式
        ...(componentData.style || {})
      }
      
      // 对于文本组件，优先显示content内容而不是组件名称
      if (componentType === 'text') {
        const displayContent = componentData.content || '这是一段文本'
        return h('div', {
          class: `component-${componentType}`,
          style: baseStyle
        }, displayContent)
      }
      
      // 赛事模块特殊处理
      if (componentType === 'pc_home_tournament') {
        // 添加背景图片样式
        const tournamentStyle = {
          ...baseStyle,
          minHeight: '400px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-pc-home-tournament`,
          style: tournamentStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'flex-start', // 上下靠上面
              alignItems: 'center' // 左右居中
            }
          }, [
            // 标题 - 居中显示，靠上方
            h('div', {
              style: {
                fontSize: '28px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '20px', // 距离顶部的距离
                textAlign: 'center'
              }
            }, componentData.title || '赛事中心'),
            // 数据源信息 - 居中显示
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '20px',
                textAlign: 'center'
              }
            }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}` : '请选择数据源')
          ])
        ])
      }
      
      // mini赛事组件特殊处理
      if (componentType === 'mini_home_tournament') {
        // 添加背景图片样式
        const tournamentStyle = {
          ...baseStyle,
          minHeight: '400px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-mini-home-tournament`,
          style: tournamentStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 左上角显示
            h('div', {
              style: {
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'left'
              }
          }, componentData.title || '赛事中心'),
            // 数据源信息
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'left',
                display: 'inline-block',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}\n数据源ID: ${componentData.dataSourceId}` : '请选择数据源')
          ])
        ]);
      }
      
      // mini学习中心组件特殊处理
      if (componentType === 'mini_home_learn') {
        // 添加背景图片样式
        const learnStyle = {
          ...baseStyle,
          minHeight: '500px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-mini-home-learn`,
          style: learnStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 左上角显示
            h('div', {
              style: {
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'left'
              }
          }, componentData.title || '学习中心'),

            // 数据源信息
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'left',
                display: 'inline-block',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}\n数据源ID: ${componentData.dataSourceId}` : '请选择数据源')
          ])
        ]);
      }
      
      // pc学习中心组件特殊处理
      if (componentType === 'pc_home_learn') {
        // 添加背景图片样式
        const pcLearnStyle = {
          ...baseStyle,
          minHeight: '500px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-pc-home-learn`,
          style: pcLearnStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 左上角显示
            h('div', {
              style: {
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'center'
              }
          }, componentData.title || '学习中心'),
            // 数据源信息
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '15px',
                margin: '20px',
                textAlign: 'center',
                display: 'inline-block',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}\n数据源ID: ${componentData.dataSourceId}` : '请选择数据源')
          ])
        ]);
      }
      
      // pc资讯中心组件特殊处理
      if (componentType === 'pc_home_information') {
        // 添加背景图片样式
        const pcInfoStyle = {
          ...baseStyle,
          minHeight: '500px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        // 确保componentData存在
        const data = componentData || {};
        
        return h('div', {
          class: `component-${componentType} bg-pc-home-information`,
          style: pcInfoStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 左上角显示
            h('div', {
              style: {
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'center'
              }
            }, componentData.title || '公告通知'),
            // 最新资讯数据源信息
            // h('div', {
            //   style: {
            //     fontSize: '14px',
            //     color: 'rgba(255,255,255,0.9)',
            //     backgroundColor: 'rgba(0,0,0,0.5)',
            //     padding: '8px 12px',
            //     borderRadius: '4px',
            //     marginTop: '15px',
            //     margin: '20px',
            //     textAlign: 'center',
            //     display: 'inline-block',
            //     whiteSpace: 'pre-line'
            //   }
            // }, componentData.dataSourceId ? `最新资讯数据源: ${componentData.dataSourceName || '未知'}\n数据源ID: ${componentData.dataSourceId}` : '请选择最新资讯数据源'),
            // 公告通知数据源信息
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '15px',
                margin: '20px',
                textAlign: 'center',
                display: 'inline-block',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceTwoId ? `公告通知数据源: ${componentData.dataSourceTwoName || '未知'}\n数据源ID: ${componentData.dataSourceTwoId}` : '请选择公告通知数据源')
          ])
        ]);
      }
      
      // pc技术支持组件特殊处理
      if (componentType === 'pc_support') {
        // 添加背景图片样式
        const pcSupportStyle = {
          ...baseStyle,
          minHeight: '500px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-pc-support`,
          style: pcSupportStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 左上角显示
            h('div', {
              style: {
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'center'
              }
          }, componentData.title || '技术支持'),
            // 数据源信息
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '15px',
                margin: '20px',
                textAlign: 'center',
                display: 'inline-block',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}\n数据源ID: ${componentData.dataSourceId}` : '请选择数据源')
          ])
        ]);
      }
      
      // pc赛事列表组件特殊处理
      if (componentType === 'pc_tournament_list') {
        // 添加背景图片样式
        const pcTournamentListStyle = {
          ...baseStyle,
          minHeight: '500px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-pc-tournament-list`,
          style: pcTournamentListStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 左上角显示
            h('div', {
              style: {
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'center'
              }
          }, componentData.title || '赛事列表'),
            // 数据源信息
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '15px',
                margin: '20px',
                textAlign: 'center',
                display: 'inline-block',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}\n数据源ID: ${componentData.dataSourceId}` : '请选择数据源')
          ])
        ]);
      }

      // pc信息展示组件特殊处理
      if (componentType === 'pc_information') {
        // 添加背景图片样式
        const pcInformationStyle = {
          ...baseStyle,
          minHeight: '500px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-pc-info`,
          style: pcInformationStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 居中显示
            h('div', {
              style: {
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                textAlign: 'center'
              }
            }, componentData.title || '信息展示'),
            
            // 最新资讯模块 - 明确区分的模块展示
            // h('div', {
            //   style: {
            //     width: '95%',
            //     backgroundColor: 'rgba(255, 255, 255, 0.1)',
            //     borderRadius: '8px',
            //     padding: '15px',
            //     marginTop: '20px',
            //     marginLeft: '20px',
            //     marginRight: '20px',
            //     textAlign: 'left'
            //   }
            // }, [
            //   h('div', {
            //     style: {
            //       fontSize: '18px',
            //       fontWeight: 'bold',
            //       color: '#13ce66',
            //       marginBottom: '10px'
            //     }
            //   }, '最新资讯'),
            //   h('div', {
            //     style: {
            //       fontSize: '14px',
            //       color: '#fff',
            //       marginBottom: '8px'
            //     }
            //   }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}` : '请选择数据源'),
            //   h('div', {
            //     style: {
            //       fontSize: '14px',
            //       color: '#fff'
            //     }
            //   }, `是否置顶前三条，轮播形式展示: ${componentData.topInfoCarousel ? '是' : '否'}`),
            //   h('div', {
            //     style: {
            //       fontSize: '12px',
            //       color: '#fff',
            //       marginTop: '10px',
            //       opacity: 0.8
            //     }
            //   })
            // ]),
            
            // 公告通知模块 - 明确区分的模块展示
            h('div', {
              style: {
                width: '95%',
                backgroundColor: 'rgba(255, 255, 255, 0.1)',
                borderRadius: '8px',
                padding: '15px',
                marginTop: '15px',
                marginLeft: '20px',
                marginRight: '20px',
                textAlign: 'left'
              }
            }, [
              h('div', {
                style: {
                  fontSize: '18px',
                  fontWeight: 'bold',
                  color: '#ff6b6b',
                  marginBottom: '10px'
                }
              }, '公告通知'),
              h('div', {
                style: {
                  fontSize: '14px',
                  color: '#fff',
                  marginBottom: '8px'
                }
              }, componentData.dataSourceTwoId ? `已选择数据源: ${componentData.dataSourceTwoName || '未知'}` : '请选择数据源'),
              h('div', {
                style: {
                  fontSize: '14px',
                  color: '#fff'
                }
              }, `是否置顶前三条，轮播形式: ${componentData.topNoticeCarousel ? '是' : '否'}`),
              h('div', {
                style: {
                  fontSize: '12px',
                  color: '#fff',
                  marginTop: '10px',
                  opacity: 0.8
                }
              })
            ])
          ])
        ]);
      }
      
      // mini资讯中心组件特殊处理
      if (componentType === 'mini_home_information') {
        // 添加背景图片样式
        const infoStyle = {
          ...baseStyle,
          minHeight: '500px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-mini-home-information`,
          style: infoStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 左上角显示
            h('div', {
              style: {
                fontSize: '24px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'left'
              }
          }, componentData.title || '资讯中心'),

            // 最新资讯数据源信息
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'left',
                display: 'inline-block',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceId ? `最新资讯数据源: ${componentData.dataSourceName || '未知'}\n数据源ID: ${componentData.dataSourceId}` : '请选择最新资讯数据源'),
            
            // 公告通知数据源信息
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '15px',
                marginLeft: '20px',
                textAlign: 'left',
                display: 'inline-block',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceTwoId ? `公告通知数据源: ${componentData.dataSourceTwoName || '未知'}\n数据源ID: ${componentData.dataSourceTwoId}` : '请选择公告通知数据源')
          ])
        ]);
      }
      
      // mini信息展示组件特殊处理
      if (componentType === 'mini_information') {
        // 添加背景图片样式
        const miniInfoStyle = {
          ...baseStyle,
          minHeight: '400px',
          padding: '0',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-mini-info`,
          style: miniInfoStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 标题 - 左上角显示
            h('div', {
              style: {
                fontSize: '20px',
                fontWeight: 'bold',
                color: '#fff',
                textShadow: '0 2px 4px rgba(0,0,0,0.3)',
                marginTop: '15px',
                marginLeft: '15px',
                textAlign: 'left'
              }
            }, componentData.title || '信息展示'),
            
            // 最新资讯模块 - 明确区分的模块展示
            // h('div', {
            //   style: {
            //     width: '90%',
            //     backgroundColor: 'rgba(255, 255, 255, 0.1)',
            //     borderRadius: '8px',
            //     padding: '12px',
            //     marginTop: '15px',
            //     marginLeft: '15px',
            //     marginRight: '15px',
            //     textAlign: 'left'
            //   }
            // }, [
            //   h('div', {
            //     style: {
            //       fontSize: '16px',
            //       fontWeight: 'bold',
            //       color: '#13ce66',
            //       marginBottom: '8px'
            //     }
            //   }, '最新资讯'),
            //   h('div', {
            //     style: {
            //       fontSize: '13px',
            //       color: '#fff',
            //       marginBottom: '6px'
            //     }
            //   }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}` : '请选择数据源'),
            //   h('div', {
            //     style: {
            //       fontSize: '13px',
            //       color: '#fff'
            //     }
            //   }, `是否置顶前三条，轮播形式展示: ${componentData.topInfoCarousel ? '是' : '否'}`)
            // ]),
            
            // 公告通知模块 - 明确区分的模块展示
            h('div', {
              style: {
                width: '90%',
                backgroundColor: 'rgba(255, 255, 255, 0.1)',
                borderRadius: '8px',
                padding: '12px',
                marginTop: '12px',
                marginLeft: '15px',
                marginRight: '15px',
                textAlign: 'left'
              }
            }, [
              h('div', {
                style: {
                  fontSize: '16px',
                  fontWeight: 'bold',
                  color: '#ff6b6b',
                  marginBottom: '8px'
                }
              }, '公告通知'),
              h('div', {
                style: {
                  fontSize: '13px',
                  color: '#fff',
                  marginBottom: '6px'
                }
              }, componentData.dataSourceTwoId ? `已选择数据源: ${componentData.dataSourceTwoName || '未知'}` : '请选择数据源'),
              h('div', {
                style: {
                  fontSize: '13px',
                  color: '#fff'
                }
              }, `是否置顶前三条，轮播形式: ${componentData.topNoticeCarousel ? '是' : '否'}`)
            ])
          ])
        ]);
      }
      
      // 赛事列表组件特殊处理
      if (componentType === 'mini_tournament_list') {
        // 添加背景图片样式
        const listStyle = {
          ...baseStyle,
          minHeight: '500px',
          padding: '0',
          className: (baseStyle.className || '') + ' bg-mini-tournament-list',
          borderRadius: '0',
          position: 'relative',
          overflow: 'hidden'
        };
        
        return h('div', {
          class: `component-${componentType} bg-mini-tournament-list`,
          style: listStyle
        }, [
          // 蒙版层 - 包裹所有内容并添加背景色
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              position: 'absolute',
              top: '0',
              left: '0',
              zIndex: '1',
              bottom: '0',
              right: '0',
              backgroundColor: 'rgba(0, 0, 0, 0.3)', // 半透明黑色蒙版
              display: 'flex',
              flexDirection: 'column'
            }
          }, [
            // 数据源信息显示
            h('div', {
              style: {
                fontSize: '14px',
                color: 'rgba(255,255,255,0.9)',
                backgroundColor: 'rgba(0, 0, 0, 0.5)',
                padding: '8px 12px',
                borderRadius: '4px',
                marginTop: '50px',
                textAlign: 'left',
                width: '100%',
                boxSizing: 'border-box',
                whiteSpace: 'pre-line'
              }
            }, componentData.dataSourceId ? `已选择数据源: ${componentData.dataSourceName || '未知'}\n数据源ID: ${componentData.dataSourceId}` : '请选择数据源')
          ])
        ]);
      }
      
      // 赛事标签组件特殊处理
      if (componentType === 'mini_tournament_tabs') {
        // Tabs组件样式
        const tabsStyle = {
          ...baseStyle,
          padding: '0',
          borderRadius: '0',
          display: 'flex',
          justifyContent: 'space-around',
          alignItems: 'center',
          border: 'none',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        };
        
        // 构建标签列表
        const tabs = [];
        
        // 我的团队 - 默认显示
        if (componentData.showMyTeam !== false) {
          tabs.push(
            h('div', { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' } }, [
              h('div', { 
                class: 'bg-tab-my-team',
                style: { width: '40px', height: '40px', marginBottom: '5px' }
              }),
              h('span', { style: { fontSize: '12px', color: '#333' } }, '我的团队')
            ])
          );
        }
        
        // 我的赛事 - 默认显示
        if (componentData.showMyTournament !== false) {
          tabs.push(
            h('div', { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' } }, [
              h('div', { 
                class: 'bg-tab-my-tournament',
                style: { width: '40px', height: '40px', marginBottom: '5px' }
              }),
              h('span', { style: { fontSize: '12px', color: '#333' } }, '我的赛事')
            ])
          );
        }
        
        // 成绩查询 - 默认显示
        if (componentData.showScoreQuery !== false) {
          tabs.push(
            h('div', { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' } }, [
              h('div', { 
                class: 'bg-tab-score-query',
                style: { width: '40px', height: '40px', marginBottom: '5px' }
              }),
              h('span', { style: { fontSize: '12px', color: '#333' } }, '成绩查询')
            ])
          );
        }
        
        return h('div', {
          class: `component-${componentType}`,
          style: tabsStyle
        }, tabs);
      }
      
      // 首页Tabs组件特殊处理
      if (componentType === 'min_home_tabs') {
        // Tabs组件样式
        const tabsStyle = {
          ...baseStyle,
          padding: '0',
          borderRadius: '0',
          display: 'flex',
          justifyContent: 'space-around',
          alignItems: 'center',
          border: 'none',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        };
        
        // 构建标签列表
        const tabs = [];
        
        // 赛事中心 - 默认显示
        if (componentData.showTournamentCenter !== false) {
          tabs.push(
            h('div', { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' } }, [
              h('div', { 
                className: 'tab-icon tournament-icon',
                style: { width: '40px', height: '40px', marginBottom: '5px' }
              }),
              h('span', { style: { fontSize: '12px', color: '#333' } }, '赛事中心')
            ])
          );
        }
        
        // 学习中心 - 默认显示
        if (componentData.showLearningCenter !== false) {
          tabs.push(
            h('div', { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' } }, [
              h('div', { 
                className: 'tab-icon learning-icon',
                style: { width: '40px', height: '40px', marginBottom: '5px' }
              }),
              h('span', { style: { fontSize: '12px', color: '#333' } }, '学习中心')
            ])
          );
        }
        
        // 资讯中心 - 默认显示
        if (componentData.showNewsCenter !== false) {
          tabs.push(
            h('div', { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' } }, [
              h('div', { 
                className: 'tab-icon news-icon',
                style: { width: '40px', height: '40px', marginBottom: '5px' }
              }),
              h('span', { style: { fontSize: '12px', color: '#333' } }, '资讯中心')
            ])
          );
        }
        
        // 技术支持 - 默认显示
        if (componentData.showTechSupport !== false) {
          tabs.push(
            h('div', { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' } }, [
              h('div', { 
                className: 'tab-icon support-icon',
                style: { width: '40px', height: '40px', marginBottom: '5px' }
              }),
              h('span', { style: { fontSize: '12px', color: '#333' } }, '技术支持')
            ])
          );
        }
        
        // 大唐杯 - 默认显示
        if (componentData.showDatangCup !== false) {
          tabs.push(
            h('div', { style: { display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' } }, [
              h('div', { 
                className: 'tab-icon datang-icon',
                style: { width: '40px', height: '40px', marginBottom: '5px' }
              }),
              h('span', { style: { fontSize: '12px', color: '#333' } }, '大唐杯')
            ])
          );
        }
        
        return h('div', {
          class: `component-${componentType}`,
          style: tabsStyle
        }, tabs);
      }
      
      // 搜索框组件特殊处理
      if (componentType === 'min_home_search') {
        // 移除背景图，直接绘制搜索框
        const searchStyle = {
          ...baseStyle,
          minHeight: '70px',
          padding: '0',
          borderRadius: '0',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        };
        
        return h('div', {
          class: `component-${componentType}`,
          style: searchStyle
        }, [
          // 搜索表单
          h('div', {
            style: {
              display: 'flex',
              alignItems: 'center',
              gap: '0',
              width: '95%',
              maxWidth: '600px',
              boxSizing: 'border-box'
            }
          }, [
            // 搜索输入框div（包含按钮）
            h('div', {
              style: {
                padding: '6px 12px',
                fontSize: '16px',
                backgroundColor: 'white',
                borderRadius: '20px', // 圆角设为0
                width: '100%',
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                border: '1px solid #dcdfe6',
                  boxSizing: 'border-box',
                justifyContent: 'space-between'
              }
            }, [
              componentData.placeholder || '请输入搜索内容',
              // 搜索按钮div
              h('div', {
                style: {
                  padding: '6px 12px',
                  fontSize: '16px',
                  backgroundColor: '#1890ff',
                  color: 'white',
                  borderRadius: '20px', // 圆角设为0
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 'bold',
                  border: '1px solid #1890ff',
                  boxSizing: 'border-box'
                }
              }, componentData.buttonText || '搜索')
            ])
          ])
        ])  
      }
      
      // 轮播图组件特殊处理 - 显示美观的轮播图demo样式
      if (componentType === 'global_banner') {
        // 获取轮播图高度，优先使用组件设置的高度，否则使用默认高度
        // 确保从正确的路径获取高度值
        const heightValue = componentData.style?.height || componentData.height;
        const bannerHeight = heightValue ? 
          (typeof heightValue === 'number' ? `${heightValue}px` : 
           (heightValue.includes('px') || heightValue.includes('%') || heightValue === 'auto') ? 
           heightValue : `${heightValue}px`) : 
          '300px';
          
        // 获取边框圆角，强制设置为0
        const borderRadius = '0';
          
        // 检查是否有自定义背景色
        const hasCustomBg = componentData.style?.backgroundColor || componentData.backgroundColor;
        const bgColor = hasCustomBg ? (componentData.style?.backgroundColor || componentData.backgroundColor) : '#6b7280';
        const textColor = hasCustomBg ? '#333' : '#ffffff';
        
        return h('div', {
          class: `component-${componentType}`,
          style: {
            ...baseStyle,
            height: bannerHeight,
            position: 'relative',
            overflow: 'hidden',
            borderRadius: borderRadius,
            boxShadow: '0 2px 12px rgba(0, 0, 0, 0.1)',
            // 覆盖任何可能的默认样式
            backgroundColor: 'transparent !important'
          }
        }, [
          // 模拟的轮播图幻灯片
          h('div', {
            style: {
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: bgColor,
              color: textColor,
              transition: 'transform 0.5s ease, background 0.3s ease',
              position: 'relative',
              // 确保内部元素也应用相同的圆角，避免溢出
              borderRadius: borderRadius
            }
          }, [
            h('div', {
              style: {
                textAlign: 'center',
                padding: '20px',
                maxWidth: '80%'
              }
            }, [
              h('h3', {
                style: {
                  fontSize: '24px',
                  fontWeight: 'bold',
                  marginBottom: '10px'
                }
              }, '轮播图'),
              h('p', {
                style: {
                  fontSize: '16px',
                  opacity: 0.9,
                  marginBottom: '20px'
                }
              }, componentData.dataSourceName || '未设置数据源'),
              h('div', {
                style: {
                  fontSize: '14px',
                  opacity: 0.8
                }
              }, componentData.dataSourceId ? `数据源ID: ${componentData.dataSourceId}` : '点击右侧属性面板设置数据源')
            ])
          ]),
          
          // 轮播指示器 - 颜色根据背景色自动调整
          h('div', {
            style: {
              position: 'absolute',
              bottom: '20px',
              left: '50%',
              transform: 'translateX(-50%)',
              display: 'flex',
              gap: '8px'
            }
          }, [
            h('div', {
              style: {
                width: '12px',
                height: '12px',
                borderRadius: '50%',
                background: hasCustomBg ? '#333' : '#333', // 根据背景色调整指示器颜色
                opacity: 1,
                cursor: 'pointer',
                boxShadow: hasCustomBg ? '0 1px 3px rgba(0, 0, 0, 0.2)' : 'none'
              }
            }),
            h('div', {
              style: {
                width: '12px',
                height: '12px',
                borderRadius: '50%',
                background: hasCustomBg ? '#333' : '#333',
                opacity: 0.5,
                cursor: 'pointer',
                boxShadow: hasCustomBg ? '0 1px 3px rgba(0, 0, 0, 0.2)' : 'none'
              }
            }),
            h('div', {
              style: {
                width: '12px',
                height: '12px',
                borderRadius: '50%',
                background: hasCustomBg ? '#333' : '#333',
                opacity: 0.5,
                cursor: 'pointer',
                boxShadow: hasCustomBg ? '0 1px 3px rgba(0, 0, 0, 0.2)' : 'none'
              }
            })
          ]),
          
          // 轮播配置信息 - 根据背景色自动调整样式
          h('div', {
            style: {
              position: 'absolute',
              bottom: '5px',
              right: '15px',
              fontSize: '11px',
              color: hasCustomBg ? 'rgba(0, 0, 0, 0.7)' : 'rgba(255, 255, 255, 0.8)',
              backgroundColor: hasCustomBg ? 'rgba(255, 255, 255, 0.7)' : 'rgba(0, 0, 0, 0.2)',
              padding: '4px 8px',
              borderRadius: '4px',
              boxShadow: hasCustomBg ? '0 1px 3px rgba(0, 0, 0, 0.1)' : 'none'
            }
          }, `自动轮播:${componentData.autoPlay ? '是' : '否'} | 间隔:${componentData.interval || 3000}ms | 循环:${componentData.loop !== false ? '是' : '否'}`)
        ])
      }
      
      // 空白组件特殊处理 - 不显示任何文本内容，并设置默认高度
      if (componentType === 'blank_spacing') {
        // 为空白组件添加默认高度，如果没有明确设置
        const blankStyle = {
          ...baseStyle,
          height: baseStyle.height || '20px',
          minHeight: undefined // 移除minHeight，优先使用height
        }
        return h('div', {
          class: `component-${componentType}`,
          style: blankStyle
        }, [])
      }
      
      // 其他组件类型的渲染逻辑
      const displayText = componentData.content || componentData.name || componentType
      return h('div', {
        class: `component-${componentType}`,
        style: baseStyle
      }, displayText)
    }
  }
};