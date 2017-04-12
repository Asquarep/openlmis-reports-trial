package mw.gov.health.lmis.settings.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import mw.gov.health.lmis.reports.web.BaseController;
import mw.gov.health.lmis.settings.domain.ConfigurationSetting;
import mw.gov.health.lmis.settings.service.ConfigurationSettingService;

@Controller
public class SettingController extends BaseController {

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  /**
   * Returns setting with given key.
   *
   * @param key Key of setting to be returned.
   * @return Configuration setting with given key.
   */
  @RequestMapping(value = "/settings/{key}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ConfigurationSetting getByKey(@PathVariable(value = "key") String key) {
    return configurationSettingService.getByKey(key);
  }
}
