package jdave.webdriver.specification;

import jdave.Block;
import jdave.ExpectationFailedException;
import jdave.IContainment;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.jmock.Expectations;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collection;

import static jdave.util.Primitives.asList;

@RunWith(JDaveRunner.class)
public class GuardingWebDriverSpec extends Specification<WebDriver> {
    private final By byName = By.name("foo");
    private MyWebDriver inner = mock(MyWebDriver.class);
    private Specification<?> spec = mock(Specification.class);
    private GuardingWebDriver driver = new GuardingWebDriver(inner, spec);

    public static interface MyWebDriver extends WebDriver, JavascriptExecutor {
    }

    public class AnyWebDriver {

        public void delegatesWebDriverCallsButFindElement() {
            checking(new Expectations() {{
                one(inner).get("url");
                one(inner).getCurrentUrl();
                one(inner).getTitle();
                one(inner).findElements(null);
                one(inner).getPageSource();
                one(inner).close();
                one(inner).quit();
                one(inner).getWindowHandle();
                one(inner).getWindowHandles();
                one(inner).switchTo();
                one(inner).navigate();
                one(inner).manage();
                one(inner).executeScript(null);
                one(inner).isJavascriptEnabled();
            }});
            driver.get("url");
            driver.getCurrentUrl();
            driver.getTitle();
            driver.findElements(null);
            driver.getPageSource();
            driver.close();
            driver.quit();
            driver.getWindowHandle();
            driver.getWindowHandles();
            driver.switchTo();
            driver.navigate();
            driver.manage();
            driver.executeScript(null);
            driver.isJavascriptEnabled();
        }
    }

    public class ElementIsNotFound {

        public void guardsFindElement() {
            checking(new Expectations() {{
                one(inner).findElements(byName);
                never(inner).findElement(byName);
                one(spec).specify(with(any(Collection.class)), with(any(IContainment.class)));
                will(throwException(new ExpectationFailedException(null)));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    driver.findElement(byName);
                }
            }, should.raise(ExpectationFailedException.class));
        }

    }

    public class ElementIsFound {
        final WebElement foundElement = mock(WebElement.class);

        public void returnsFoundElement() {
            checking(new Expectations() {{
                one(inner).findElements(byName);
                will(returnValue(asList(new WebElement[]{foundElement})));
                never(inner).findElement(byName);
                one(spec).specify(with(any(Collection.class)), with(any(IContainment.class)));
            }});
            specify(driver.findElement(byName), equal(foundElement));
        }
    }

}
