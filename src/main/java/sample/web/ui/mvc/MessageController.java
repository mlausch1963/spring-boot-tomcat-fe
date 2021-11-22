/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.web.ui.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import sample.web.ui.Message;
import sample.web.ui.MessageRepository;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.core.annotation.Timed;
import org.apache.commons.math3.distribution.ParetoDistribution;

import java.util.logging.Logger;

/**
 * @author Rob Winch
 * @author Doo-Hwan Kwak
 * @author Michael Lausch
 * A Controller class with a doit method which allows to create specific
 * error rates and delays.
 */
@Controller
@Timed(percentiles = {0.5, 0.75, 0.95, 0.99})
@RequestMapping("/")
class MessageController {

	@Autowired
    private final MessageRepository messageRepository;

	@Autowired
	private final SomeClient someClient;
	
	
    private final ParetoDistribution paretoGenerator = new ParetoDistribution(0.005, 6);
    static Logger logger = Logger.getLogger(MessageController.class.getName());

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        this.someClient = new SomeClient();
    }

	@GetMapping
	public ModelAndView list() {
		Iterable<Message> messages = this.messageRepository.findAll();
		return new ModelAndView("messages/list", "messages", messages);
	}

	@GetMapping("{id}")
	public ModelAndView view(@PathVariable("id") Message message) {
		return new ModelAndView("messages/view", "message", message);
	}

    @GetMapping(params = "form")
//	@Timed(value = "long_create_form", longTask = true)
    @Async(value="myThreadPoolExecutor")
    public String createForm(@ModelAttribute Message message) {
        return "messages/form";
    }

	@PostMapping
//	@Timed(value = "long_create", longTask = true)
	public ModelAndView create(@Validated Message message, BindingResult result,
			RedirectAttributes redirect) {
		if (result.hasErrors()) {
			return new ModelAndView("messages/form", "formErrors", result.getAllErrors());
		}
		message = this.messageRepository.save(message);
		redirect.addFlashAttribute("globalMessage", "Successfully created a new message");
		return new ModelAndView("redirect:/{message.id}", "message.id", message.getId());
	}

	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}

	@GetMapping(value = "delete/{id}")
	public ModelAndView delete(@PathVariable("id") Long id) {
		this.messageRepository.deleteMessage(id);
		Iterable<Message> messages = this.messageRepository.findAll();
		return new ModelAndView("messages/list", "messages", messages);
	}

	@GetMapping(value = "modify/{id}")
	public ModelAndView modifyForm(@PathVariable("id") Message message) {
		return new ModelAndView("messages/form", "message", message);
	}

    /**
     *
     * @param reliability from 0 to 1. 0 means 0% success, 1 means 100% success.
     * @param megabytes Allocate that many megabytes of memory and write to it.
     * @return
     *
     * The function first checks if it should succeed or not. If not, return immediatly with an error.
     * If it should succeed, calculate a latency using a pareto distribution and cap it at 4 seconds.
     * Allocate the memory, dirty it and let the thread sleep for so long, write the to the memory
     * and and finally return the response.
     *
     */
    @GetMapping(value = "doit")
    @Timed(extraTags = {"api", "debugging"})
    public ResponseEntity<?> doit(@RequestParam(defaultValue = "0.95") Double reliability,
                                  @RequestParam(defaultValue = "30") Long megabytes) {
        
    	double failureProbability = Math.random();
        if (failureProbability > reliability) {
            someClient.fail();
            return new ResponseEntity<>("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        someClient.success();
        
        byte[] mem = new byte[(int) (megabytes * 1024 * 1024)];
        for (int i = 0; i < megabytes; i++) {
            mem[i] = (byte) 0xff;
        }
        double time = paretoGenerator.sample();

        time *= 1000.0;

        if (time > 4000.0) {
            time = 4000.0;
        }
        try {
            Thread.sleep((long) time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < megabytes; i++) {
            mem[i] = 0x00;
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
    
    @GetMapping("favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
    }
}
