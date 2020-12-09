package org.vision.core.services.http;

import com.alibaba.fastjson.JSONObject;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vision.api.GrpcAPI.BytesMessage;
import org.vision.core.Wallet;
import org.vision.protos.contract.SmartContractOuterClass.SmartContractDataWrapper;


@Component
@Slf4j(topic = "API")
public class GetContractInfoServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  private static final String VALUE = "value";

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);
      String input = request.getParameter(VALUE);
      if (visible) {
        input = Util.getHexAddress(input);
      }

      JSONObject jsonObject = new JSONObject();
      jsonObject.put(VALUE, input);
      BytesMessage.Builder build = BytesMessage.newBuilder();
      JsonFormat.merge(jsonObject.toJSONString(), build, visible);
      SmartContractDataWrapper smartContract = wallet.getContractInfo(build.build());
      JSONObject jsonSmartContract = JSONObject
          .parseObject(JsonFormat.printToString(smartContract, visible));
      response.getWriter().println(jsonSmartContract.toJSONString());
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(input);
      boolean visible = Util.getVisiblePost(input);
      if (visible) {
        JSONObject jsonObject = JSONObject.parseObject(input);
        String value = jsonObject.getString(VALUE);
        jsonObject.put(VALUE, Util.getHexAddress(value));
        input = jsonObject.toJSONString();
      }

      BytesMessage.Builder build = BytesMessage.newBuilder();
      JsonFormat.merge(input, build, visible);
      SmartContractDataWrapper smartContract = wallet.getContractInfo(build.build());
      JSONObject jsonSmartContract = JSONObject
          .parseObject(JsonFormat.printToString(smartContract, visible));
      response.getWriter().println(jsonSmartContract.toJSONString());
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
