package jdave.webdriver.specification;

import jdave.BaseMatcherContainment;
import jdave.Specification;
import org.hamcrest.StringDescription;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GuardingWebDriver implements WebDriver, JavascriptExecutor {

    private final WebDriver driver;
    private final Specification<?> spec;

    public GuardingWebDriver(WebDriver driver, Specification<?> spec) {
        this.driver = driver;
        this.spec = spec;
    }

    private JavascriptExecutor asJavascriptExecutor() {
        return (JavascriptExecutor) driver;
    }

    public void get(String url) {
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    public WebElement findElement(final By by) {
        List<WebElement> foundElements = driver.findElements(by);
        spec.specify(foundElements, new BaseMatcherContainment<WebElement>() {
            public boolean matches(Collection<WebElement> actual) {
                return !actual.isEmpty();
            }

            public String error(Collection<WebElement> actual) {
                return new StringDescription()
                        .appendText("The element specified ")
                        .appendText(by.toString())
                        .appendText(" does not exist.")
                        .toString();
            }
        });
        return foundElements.get(0);
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    public void close() {
        driver.close();
    }

    public void quit() {
        driver.quit();
    }

    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    public TargetLocator switchTo() {
        return driver.switchTo();
    }

    public Navigation navigate() {
        return driver.navigate();
    }

    public Options manage() {
        return driver.manage();
    }

    public Object executeScript(String script, Object... args) {
        return asJavascriptExecutor().executeScript(script, args);
    }

    public boolean isJavascriptEnabled() {
        return asJavascriptExecutor().isJavascriptEnabled();
    }
}
