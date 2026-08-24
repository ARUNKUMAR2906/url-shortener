package com.ap01.url_shortener.controller;

import com.ap01.url_shortener.utils.Base62;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class baseController {

    @GetMapping("/base")
    public void baseController() {
        System.out.println(Base62.encode(0));
        System.out.println(Base62.encode(9));
        System.out.println(Base62.encode(1));
        System.out.println(Base62.encode(10));
        System.out.println(Base62.encode(61));
        System.out.println(Base62.encode(62));
        System.out.println(Base62.encode(125));
        System.out.println(Base62.encode(3844));
        System.out.println(Base62.decode(Base62.encode(0)));
        System.out.println(Base62.decode(Base62.encode(9)));
        System.out.println(Base62.decode(Base62.encode(1)));
        System.out.println(Base62.decode(Base62.encode(10)));
        System.out.println(Base62.decode(Base62.encode(61)));
        System.out.println(Base62.decode(Base62.encode(62)));
        System.out.println(Base62.decode(Base62.encode(125)));
        System.out.println(Base62.decode(Base62.encode(3844)));
    }
}
