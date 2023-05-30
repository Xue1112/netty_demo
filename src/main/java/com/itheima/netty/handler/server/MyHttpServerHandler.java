package com.itheima.netty.handler.server;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @description
 * @author: ts
 * @create:2021-06-05 18:43
 */
@Slf4j
public class MyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE);

    static {
        DiskFileUpload.baseDirectory = "/opt/netty/fileupload";
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        /**
         * 1: 根据不同的请求方式做出处理
         */
        HttpMethod method = fullHttpRequest.method();
        if (HttpMethod.GET.equals(method)) {
            parseGet(fullHttpRequest);
        }else if (HttpMethod.POST.equals(method)) {
            parsePost(fullHttpRequest);
        }else {
            log.error("{} method is not supported ,please change http method for get or post!",method);
        }


        //给客户端做出响应
        //response client
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h3>success</h3>");
        sb.append("</body>");
        sb.append("</html>");
        writeResponse(ctx,fullHttpRequest,HttpResponseStatus.OK,sb.toString());
    }


    private void writeResponse(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest, HttpResponseStatus status, String msg) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,status);
        response.content().writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=utf-8");
        HttpUtil.setContentLength(response,response.content().readableBytes());
        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION,"keep-alive");
            ctx.writeAndFlush(response);
        }else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void parsePost(FullHttpRequest fullHttpRequest) {
        /**
         * post请求要按照content-type来进行解析
         */
        String contentType = getContentType(fullHttpRequest);
        switch (contentType) {
            case "application/json":
                parseJson(fullHttpRequest);
                break;
            case "application/x-www-form-urlencoded":
                parseFormData(fullHttpRequest);
                break;
            case "multipart/form-data":
                parseMultipart(fullHttpRequest);
                break;
            default:
        }
    }

    /**
     * 在该方法中的解析方式，同样也适用于解析普通的表单提交请求
     * 通用（普通post，文件上传）
     * @param fullHttpRequest
     */
    private void parseMultipart(FullHttpRequest fullHttpRequest) {
        HttpPostRequestDecoder postRequestDecoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, fullHttpRequest);
        //判断是否是multipart
        if (postRequestDecoder.isMultipart()) {
            //获取body中的数据
            List<InterfaceHttpData> bodyHttpDatas = postRequestDecoder.getBodyHttpDatas();
            bodyHttpDatas.forEach(dataItem ->{
                //判断表单项的类型
                InterfaceHttpData.HttpDataType httpDataType = dataItem.getHttpDataType();
                if (httpDataType.equals(InterfaceHttpData.HttpDataType.Attribute)) {
                    //普通表单项，直接获取数据
                    Attribute attribute = (Attribute) dataItem;
                    try {
                        log.info("表单项名称:{},表单项值:{}",attribute.getName(),attribute.getValue());
                    } catch (IOException e) {
                        log.error("获取表单项数据错误,msg={}",e.getMessage());
                    }
                }else if (httpDataType.equals(InterfaceHttpData.HttpDataType.FileUpload)) {
                    //文件上传项，将文件保存到磁盘
                    FileUpload fileUpload = (FileUpload) dataItem;
                    //获取原始文件名称
                    String filename = fileUpload.getFilename();
                    //获取表单项名称
                    String name = fileUpload.getName();
                    log.info("文件名称:{},表单项名称:{}",filename,name);
                    //将文件保存到磁盘
                    if (fileUpload.isCompleted()) {
                        try {
                            String path = DiskFileUpload.baseDirectory + File.separator + filename;
                            //File file = fileUpload.getFile();
                            fileUpload.renameTo(new File(path));
                        } catch (IOException e) {
                            log.error("文件转存失败,msg={}",e.getMessage());
                        }
                    }
                }
            });
        }
    }

    private void parseFormData(FullHttpRequest fullHttpRequest) {
        //两个部分有数据  uri，body
        parseKVstr(fullHttpRequest.uri(),true);
        parseKVstr(fullHttpRequest.content().toString(StandardCharsets.UTF_8),false);
    }

    private void parseJson(FullHttpRequest fullHttpRequest) {
        String jsonstr = fullHttpRequest.content().toString(StandardCharsets.UTF_8);
        //使用json工具反序列化
        JSONObject jsonObject = JSONObject.parseObject(jsonstr);
        //打印 json数据
        jsonObject.entrySet().stream().forEach(entry ->{
            log.info("json key={},json value={}",entry.getKey(),entry.getValue());
        });
    }

    private String getContentType(FullHttpRequest fullHttpRequest) {
        HttpHeaders headers = fullHttpRequest.headers();
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);// content-type: text/plain;charset=utf-8
        return contentType.split(";")[0];
    }

    private void parseGet(FullHttpRequest fullHttpRequest) {
        //通过请求url解析获得参数数据
        parseKVstr(fullHttpRequest.uri(),true);
    }

    private void parseKVstr(String str,boolean hashPath) {
        // 通过QueryStringDecoder解析kv字符串
        QueryStringDecoder qsd  = new QueryStringDecoder(str, StandardCharsets.UTF_8,hashPath);
        Map<String, List<String>> parameters = qsd.parameters();
        //封装参数，执行业务 此处打印即可
        if (parameters!=null && parameters.size() > 0) {
            parameters.entrySet().stream().forEach(entry->{
                log.info("参数名:{},参数值:{}",entry.getKey(),entry.getValue());
            });
        }

    }
}
