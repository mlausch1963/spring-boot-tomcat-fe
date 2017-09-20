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

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sample.web.ui.Message;
import sample.web.ui.MessageRepository;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.core.annotation.Timed;
import org.apache.commons.math3.distribution.ParetoDistribution;

import java.util.logging.Logger;

/**
 * @author Rob Winch
 * @author Doo-Hwan Kwak
 */
@Controller
@Timed()
@RequestMapping("/")
public class MessageController {

	private final MessageRepository messageRepository;
    private ParetoDistribution paretoGenerator = new ParetoDistribution(0.005, 6);
    static Logger logger = Logger.getLogger(MessageController.class.getName());

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
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
	public String createForm(@ModelAttribute Message message) {
		return "messages/form";
	}

	@PostMapping
//	@Timed(value = "long_create", longTask = true)
	public ModelAndView create(@Valid Message message, BindingResult result,
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

    @GetMapping(value = "doit")
    public ResponseEntity<?> doit(@RequestParam(defaultValue = "0.99") Double reliability,
                                  @RequestParam(defaultValue = "0") Long megabytes) {
        double failureProbability = Math.random();
        if (failureProbability > reliability) {
            return new ResponseEntity<>("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
        }

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
}
