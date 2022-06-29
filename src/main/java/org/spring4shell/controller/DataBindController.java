package org.spring4shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring4shell.controller.dto.AssertRequest;
import org.spring4shell.controller.dto.Request;
import org.springframework.beans.EvalTreePrinter;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataBindController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBindController.class);

    // Example of dependency injection
    private final ConversionService conversionService;

    DataBindController(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/bean")
    public void getBean(final Request request) {
        LOGGER.info("GET request received: {}", request);
    }

    @PostMapping("/bean")
    public void postBean(@ModelAttribute final Request request) {
        LOGGER.info("POST request received: {}", request);
    }

    @PostMapping("/json")
    public void postJsonBean(@RequestBody final Request request) {
        LOGGER.info("POST JSON request received: {}", request);
    }

    @GetMapping("/tree")
    public void tree() throws Exception {
        LOGGER.info("Printing eval tree");

        new EvalTreePrinter(new Request(), conversionService).print();
    }

    @PostMapping("/assert")
    public void assertBean(final AssertRequest assertRequest) {
        LOGGER.info("POST ASSERT request received: {}", assertRequest);
        assertRequest.assertName("dreamtheater");
    }

    //@InitBinder
    //public void initBinder(WebDataBinder binder) {
    //    // mitigation:
    //    String[] blackList = { "class.*", "Class.*", "*.class.*", ".*Class.*" };
    //    binder.setDisallowedFields(blackList);
    //}
}
